package com.example.banking.interbank.adapter.in.messaging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.in.TransferOrderEvent;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import com.example.banking.interbank.domain.order.OrderStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class TransferOrderEventConsumerTest {

    private static final String TOPIC_PAYLOAD = """
            {"orderId":114,"customerUserId":"g2f01259-secret","eventDate":"2022-03-26T00:00:00",
             "status":"PENDING_SEND","nextStatus":"FINAL_APPROVAL","channel":"business banking",
             "action":"TO_PENDING","unknownField":"ignored"}""";

    private final List<TransferOrderEvent> events = new ArrayList<>();
    private final List<RequestContext> contexts = new ArrayList<>();
    private final AtomicInteger subscriptions = new AtomicInteger();
    private final AtomicReference<Object> mdc = new AtomicReference<>();
    private final AtomicReference<Throwable> useCaseFailure = new AtomicReference<>();
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private final Logger logger = (Logger) LoggerFactory.getLogger(TransferOrderEventConsumer.class);

    private final TransferOrderEventConsumer consumer = new TransferOrderEventConsumer((event, ctx) ->
            Mono.deferContextual(context -> {
                subscriptions.incrementAndGet();
                events.add(event);
                contexts.add(ctx);
                mdc.set(context.getOrDefault("MDC_CTX", null));
                return useCaseFailure.get() == null ? Mono.<Void>empty() : Mono.<Void>error(useCaseFailure.get());
            }));

    @BeforeEach
    void captureLogs() {
        logs.start();
        logger.addAppender(logs);
    }

    @AfterEach
    void detach() {
        logger.detachAppender(logs);
    }

    private static Message<String> message(String payload, Map<String, Object> headers) {
        return MessageBuilder.withPayload(payload).copyHeaders(headers).build();
    }

    private String allLogs() {
        return logs.list.stream().map(e -> e.getFormattedMessage() + " " + e.getThrowableProxy())
                .reduce("", String::concat);
    }

    @Test
    void should_parse_a_topic_payload_without_a_retry_counter_and_call_the_use_case_once() {
        consumer.accept(message(TOPIC_PAYLOAD, Map.of()));

        assertThat(subscriptions).hasValue(1);
        assertThat(events).singleElement().isEqualTo(new TransferOrderEvent(114L, "g2f01259-secret",
                "2022-03-26T00:00:00", "PENDING_SEND", OrderStatus.of("FINAL_APPROVAL"), "business banking",
                "TO_PENDING", null));
    }

    @Test
    void should_read_the_retry_counter_the_publisher_writes() {
        consumer.accept(message("{\"orderId\":1,\"nextStatus\":\"PENDING\",\"retrySeconds\":60}", Map.of()));

        assertThat(events.get(0).retrySeconds()).isEqualTo(60);
    }

    @Test
    void should_still_read_the_counter_under_its_previous_name() {
        consumer.accept(message("{\"orderId\":1,\"nextStatus\":\"PENDING\",\"segIntervalError\":90}", Map.of()));

        assertThat(events.get(0).retrySeconds()).isEqualTo(90);
    }

    @Test
    void should_map_every_header_by_its_value() {
        consumer.accept(message(TOPIC_PAYLOAD, Map.of("app-name", "Banking", "request-id", "req-7",
                "x-session", "session-1", "x-language", "EN", "customer-affiliation-id", "aff-1",
                "customer-user-id", "user-1")));

        assertThat(contexts).singleElement().isEqualTo(new RequestContext("req-7", null, "Banking", "session-1",
                "EN", "aff-1", "user-1", "business banking", "TO_PENDING"));
    }

    @Test
    void should_decode_headers_that_arrive_as_bytes() {
        consumer.accept(message(TOPIC_PAYLOAD, Map.of("request-id", "req-7".getBytes(StandardCharsets.UTF_8),
                "x-language", "EN".getBytes(StandardCharsets.UTF_8))));

        assertThat(contexts.get(0).requestId()).isEqualTo("req-7");
        assertThat(contexts.get(0).language()).isEqualTo("EN");
    }

    @Test
    void should_leave_absent_headers_empty_and_generate_one_prefixed_request_id() {
        consumer.accept(message(TOPIC_PAYLOAD, Map.of()));

        RequestContext ctx = contexts.get(0);
        assertThat(ctx.requestId()).startsWith("def-").hasSizeGreaterThan(10);
        assertThat(ctx.session()).isNull();
        assertThat(ctx.language()).isNull();
        assertThat(ctx.customerUserId()).isNull();
    }

    @Test
    void should_put_the_request_id_in_the_reactor_mdc_context() {
        consumer.accept(message(TOPIC_PAYLOAD, Map.of("request-id", "req-7")));

        assertThat(mdc.get()).isEqualTo(Map.of("request-id", "req-7"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not json at all", "{\"orderId\":", "", "[]"})
    void should_discard_an_unreadable_message_with_a_log_and_without_calling_the_use_case(String payload) {
        assertThatCode(() -> consumer.accept(message(payload, Map.of()))).doesNotThrowAnyException();

        assertThat(subscriptions).hasValue(0);
        assertThat(logs.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel().toString()).isEqualTo("WARN");
            assertThat(event.getFormattedMessage()).containsIgnoringCase("discard");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"orderId\":1}",
            "{\"orderId\":1,\"nextStatus\":\"\"}",
            "{\"orderId\":1,\"nextStatus\":null}",
            "{\"nextStatus\":\"PENDING\"}"})
    void should_discard_a_message_without_next_status_or_order_id(String payload) {
        consumer.accept(message(payload, Map.of()));

        assertThat(subscriptions).hasValue(0);
        assertThat(logs.list).singleElement()
                .satisfies(event -> assertThat(event.getFormattedMessage()).containsIgnoringCase("discard"));
    }

    @Test
    void should_log_a_use_case_failure_without_throwing_and_without_sensitive_data() {
        useCaseFailure.set(new TransferStepFailedException("republish",
                new IllegalStateException("session-1 g2f01259-secret 98765.43")));

        assertThatCode(() -> consumer.accept(message(TOPIC_PAYLOAD, Map.of("x-session", "session-1"))))
                .doesNotThrowAnyException();

        assertThat(allLogs()).contains("republish").doesNotContain("g2f01259-secret", "session-1", "98765");
    }

    @Test
    void should_not_log_payload_or_header_values_for_a_processed_message() {
        consumer.accept(message(TOPIC_PAYLOAD, Map.of("x-session", "session-1", "customer-user-id", "user-1")));

        assertThat(allLogs()).doesNotContain("g2f01259-secret", "session-1", "user-1");
    }
}
