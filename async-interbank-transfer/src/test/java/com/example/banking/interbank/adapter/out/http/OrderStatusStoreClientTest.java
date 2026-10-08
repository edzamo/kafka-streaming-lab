package com.example.banking.interbank.adapter.out.http;

import com.example.banking.interbank.adapter.out.http.generated.spInterbankTransferOrder.api.InterbankTransferOrderApi;
import com.example.banking.interbank.adapter.out.http.generated.spInterbankTransferOrder.invoker.ApiClient;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;

import static com.example.banking.interbank.adapter.out.http.HttpFixture.CTX;
import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusStoreClientTest {

    private final HttpFixture http = new HttpFixture();
    private final OrderStatusStoreClient client = new OrderStatusStoreClient(
            new InterbankTransferOrderApi(new ApiClient(http.webClient()).setBasePath(http.baseUrl())), http.calls);

    @AfterEach
    void tearDown() throws Exception {
        http.close();
    }

    @Test
    void should_patch_the_status_with_the_traceability_headers() throws Exception {
        http.respond(200, "{}");

        StepVerifier.create(client.updateStatus(114L, "FINAL_APPROVAL", null, CTX)).verifyComplete();

        RecordedRequest request = http.takeRequest();
        assertThat(request.getMethod()).isEqualTo("PATCH");
        assertThat(request.getPath()).isEqualTo("/api/v1/interbank-transfer-orders");
        assertThat(request.getHeader("request-id")).isEqualTo("req-1");
        assertThat(request.getHeader("process-code")).isEqualTo("INTERBANK04");
        assertThat(request.getHeader("app-name")).isEqualTo("Banking");
        assertThat(request.getHeader("caller-name")).isEqualTo("async-interbank-transfer");
        assertThat(request.getHeader("x-session")).isEqualTo("session-secret");
        assertThat(request.getHeader("customer-user-id")).isEqualTo("user-secret");
        assertThat(request.getHeader("customer-affiliation-id")).isEqualTo("aff-secret");
        var body = HttpFixture.json(request);
        assertThat(body.path("trayOrderId").asText()).isEqualTo("114");
        assertThat(body.path("status").path("code").asText()).isEqualTo("FINAL_APPROVAL");
        assertThat(body.path("executionDate").isNull() || body.path("executionDate").isMissingNode()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "FINAL_APPROVAL", "PENDING_SEND", "ERROR"})
    void should_send_each_status_code_untouched(String status) throws Exception {
        http.respond(200, "{}");

        StepVerifier.create(client.updateStatus(1L, status, null, CTX)).verifyComplete();

        assertThat(HttpFixture.json(http.takeRequest()).path("status").path("code").asText()).isEqualTo(status);
    }

    @Test
    void should_send_the_execution_date_when_given() throws Exception {
        http.respond(200, "{}");

        StepVerifier.create(client.updateStatus(1L, "ERROR", OffsetDateTime.parse("2026-01-01T10:00:00Z"), CTX))
                .verifyComplete();

        assertThat(HttpFixture.json(http.takeRequest()).path("executionDate").asText()).startsWith("2026-01-01T10:00:00");
    }

    @Test
    void should_translate_an_http_error_into_a_step_failure() {
        http.respond(500, "{\"message\":\"boom\"}");

        StepVerifier.create(client.updateStatus(1L, "ERROR", null, CTX))
                .expectErrorSatisfies(error -> assertThat(error).isInstanceOf(TransferStepFailedException.class)
                        .extracting(e -> ((TransferStepFailedException) e).step()).isEqualTo("updateStatus"))
                .verify();
    }
}
