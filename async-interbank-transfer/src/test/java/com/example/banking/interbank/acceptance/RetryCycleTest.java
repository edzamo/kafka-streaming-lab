package com.example.banking.interbank.acceptance;

import com.example.banking.interbank.InterbankTransferApplication;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Whole retry cycle through the real binder, consumer, use case and HTTP/messaging adapters: the first delivery
 * fails on the second item, the event is republished, delivered again, and every downstream call of both
 * attempts must carry the same request id so the executor can deduplicate item 0.
 */
@SpringBootTest(classes = InterbankTransferApplication.class)
@Import({TestChannelBinderConfiguration.class, RetryCycleTest.VirtualTime.class})
@ActiveProfiles("test")
class RetryCycleTest {

    private static final String TOPIC = "interbank-topic";
    private static final VirtualTimeScheduler SCHEDULER = VirtualTimeScheduler.create();
    private static final MockWebServer SERVER = new MockWebServer();
    private static final List<RecordedRequest> REQUESTS = new CopyOnWriteArrayList<>();
    private static final AtomicInteger TRANSFER_POSTS = new AtomicInteger();

    private static final String PAYLOAD = """
            {"orderId":114,"customerUserId":"u-1","eventDate":"2022-03-26T00:00:00","status":"APPROVAL",
             "nextStatus":"FINAL_APPROVAL","channel":"web","action":"APPROVE"}""";
    private static final String ORDER = """
            {"customerAffiliationId":"aff-9","customer":{"fullName":"Jane Doe"},
             "sourceProduct":{"productNumber":"SRC-1"},
             "interbankTransferOrderItems":[
               {"amount":10,"currency":{"code":"USD"},"destinationProduct":{"productNumber":"DST-1"}},
               {"amount":20,"currency":{"code":"USD"},"destinationProduct":{"productNumber":"DST-2"}}]}""";

    @TestConfiguration
    static class VirtualTime {
        @Bean
        @Primary
        Scheduler testScheduler() {
            return SCHEDULER;
        }
    }

    @DynamicPropertySource
    static void downstream(DynamicPropertyRegistry registry) throws IOException {
        SERVER.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                REQUESTS.add(request);
                String path = request.getRequestUrl().encodedPath();
                if (path.endsWith("/bs/interbank-transfer-orders")) {
                    return json(200, ORDER);
                }
                if (path.endsWith("/bs/interbank-transfer")) {
                    // the second POST is item 1 of the first attempt
                    return TRANSFER_POSTS.incrementAndGet() == 2 ? json(500, "{}") : json(200, "");
                }
                return json(200, "{}");
            }
        });
        SERVER.start();
        registry.add("CLUSTER_PATH_SP_INTERBANK_TRANSFER_ORDER", () -> SERVER.url("/sp/").toString());
        registry.add("CLUSTER_PATH_BS_INTERBANK_TRANSFER_ORDER", () -> SERVER.url("/bs/").toString());
        registry.add("CLUSTER_PATH_BS_ORDER", () -> SERVER.url("/bs-order/").toString());
    }

    private static MockResponse json(int status, String body) {
        return new MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(body);
    }

    @AfterAll
    static void stopServer() throws IOException {
        SERVER.shutdown();
    }

    @Autowired
    InputDestination input;
    @MockitoBean
    ReactiveStringRedisTemplate redis;

    @BeforeEach
    void reset() {
        REQUESTS.clear();
        TRANSFER_POSTS.set(0);
        when(redis.convertAndSend(anyString(), anyString())).thenReturn(Mono.just(1L));
    }

    /**
     * The test binder loops what the publisher writes to the topic back into the consumer, like the real broker
     * does; the second delivery then waits its 30 virtual seconds on the injected scheduler.
     */
    private void runCycle(Map<String, Object> headers) {
        input.send(MessageBuilder.withPayload(PAYLOAD).copyHeaders(headers).build(), TOPIC);
        await().atMost(Duration.ofSeconds(10)).until(() -> SCHEDULER.getScheduledTaskCount() > 0);
        SCHEDULER.advanceTimeBy(Duration.ofSeconds(30));
        await().atMost(Duration.ofSeconds(10)).until(() -> requestsTo("/bs-order/orders/114").size() == 1);
    }

    private List<RecordedRequest> requestsTo(String pathSuffix) {
        return REQUESTS.stream().filter(r -> r.getRequestUrl().encodedPath().endsWith(pathSuffix)).toList();
    }

    @Test
    void should_send_the_same_idempotency_key_per_item_on_the_retry_when_the_message_carries_a_request_id() {
        runCycle(Map.of("request-id", "req-9", "x-session", "session-1"));

        assertThat(requestsTo("/bs/interbank-transfer")).extracting(r -> r.getHeader("Idempotency-Key"))
                .containsExactly("req-9-0", "req-9-1", "req-9-0", "req-9-1");
        assertThat(REQUESTS).extracting(r -> r.getHeader("request-id")).containsOnly("req-9");
        assertThat(REQUESTS).extracting(r -> r.getHeader("x-session")).containsOnly("session-1");
    }

    @Test
    void should_generate_the_request_id_once_and_keep_it_across_the_retry_when_the_message_has_none() {
        runCycle(Map.of());

        List<String> keys = requestsTo("/bs/interbank-transfer").stream()
                .map(r -> r.getHeader("Idempotency-Key")).toList();
        String requestId = requestsTo("/bs-order/orders/114").get(0).getHeader("request-id");
        assertThat(requestId).startsWith("def-");
        assertThat(keys).containsExactly(requestId + "-0", requestId + "-1", requestId + "-0", requestId + "-1");
        assertThat(REQUESTS).extracting(r -> r.getHeader("request-id")).containsOnly(requestId);
    }
}
