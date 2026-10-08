package com.example.banking.interbank.adapter.out.http;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.banking.interbank.adapter.out.http.generated.spInterbankTransferOrder.api.InterbankTransferOrderApi;
import com.example.banking.interbank.adapter.out.http.generated.spInterbankTransferOrder.invoker.ApiClient;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static com.example.banking.interbank.adapter.out.http.HttpFixture.CTX;
import static org.assertj.core.api.Assertions.assertThat;

class DownstreamCallsTest {

    private final HttpFixture http = new HttpFixture();
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private final Logger logger = (Logger) LoggerFactory.getLogger(DownstreamCalls.class);

    private OrderStatusStoreClient clientWithTimeout(Duration timeout) {
        var calls = new DownstreamCalls(http.defaults, http.clock, timeout);
        return new OrderStatusStoreClient(
                new InterbankTransferOrderApi(new ApiClient(http.webClient()).setBasePath(http.baseUrl())), calls);
    }

    @BeforeEach
    void captureLogs() {
        logs.start();
        logger.addAppender(logs);
    }

    @AfterEach
    void tearDown() throws Exception {
        logger.detachAppender(logs);
        http.close();
    }

    @Test
    void should_fail_the_step_with_a_timeout_when_the_service_does_not_answer() {
        http.server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        StepVerifier.create(clientWithTimeout(Duration.ofMillis(300)).updateStatus(1L, "ERROR", null, CTX))
                .expectErrorSatisfies(error -> assertThat(error).isInstanceOf(TransferStepFailedException.class)
                        .hasCauseInstanceOf(TimeoutException.class))
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void should_log_the_failed_step_and_status_but_nothing_sensitive() {
        http.respond(500, "{\"detail\":\"session-secret aff-secret user-secret 98765.43\"}");

        StepVerifier.create(clientWithTimeout(Duration.ofSeconds(2)).updateStatus(1L, "ERROR", null, CTX))
                .expectError(TransferStepFailedException.class).verify();

        assertThat(logs.list).singleElement().satisfies(event -> {
            assertThat(event.getFormattedMessage()).contains("updateStatus").contains("HTTP 500");
            assertThat(event.getFormattedMessage() + event.getThrowableProxy())
                    .doesNotContain("session-secret", "aff-secret", "user-secret", "98765");
        });
    }
}
