package com.example.banking.interbank.adapter.out.http;

import com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.api.InterbankTransferApi;
import com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.invoker.ApiClient;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import com.example.banking.interbank.domain.order.TransferInstruction;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static com.example.banking.interbank.adapter.out.http.HttpFixture.CTX;
import static org.assertj.core.api.Assertions.assertThat;

class TransferExecutorClientTest {

    private final HttpFixture http = new HttpFixture();
    private final TransferExecutorClient client = new TransferExecutorClient(
            new InterbankTransferApi(new ApiClient(http.webClient()).setBasePath(http.baseUrl())), http.calls);
    private final TransferInstruction instruction =
            new TransferInstruction(114L, "SRC-1", "DST-1", "USD", new BigDecimal("1000"), "Jane Doe");

    @AfterEach
    void tearDown() throws Exception {
        http.close();
    }

    @Test
    void should_post_the_transfer_request_built_from_the_instruction() throws Exception {
        http.respond(200, "");

        StepVerifier.create(client.execute(instruction, CTX, 0)).verifyComplete();

        RecordedRequest request = http.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/api/v1/interbank-transfer");
        var body = HttpFixture.json(request);
        assertThat(body.path("sourceProduct").path("productId").asText()).isEqualTo("SRC-1");
        assertThat(body.path("destinationProduct").path("productId").asText()).isEqualTo("DST-1");
        assertThat(body.path("beneficiary").path("fullName").asText()).isEqualTo("Jane Doe");
        assertThat(body.path("beneficiary").path("email").asText()).isEmpty();
        assertThat(body.path("amount").asText()).isEqualTo("1000");
        assertThat(body.path("currency").path("code").asText()).isEqualTo("USD");
        assertThat(body.path("description").asText()).isEmpty();
        assertThat(body.path("orderId").asInt()).isEqualTo(114);
        assertThat(body.path("action").path("code").asText()).isEqualTo("APPROVE");
    }

    @Test
    void should_send_the_traceability_gateway_and_idempotency_headers() throws Exception {
        http.respond(200, "");

        StepVerifier.create(client.execute(instruction, CTX, 2)).verifyComplete();

        RecordedRequest request = http.takeRequest();
        assertThat(request.getHeader("Idempotency-Key")).isEqualTo("req-1-2");
        assertThat(request.getHeader("X-Guid")).isEqualTo("req-1");
        assertThat(request.getHeader("X-Agency")).isEqualTo("agency-1");
        assertThat(request.getHeader("X-Geolocation")).isEqualTo("geo-1");
        assertThat(request.getHeader("request-id")).isEqualTo("req-1");
        assertThat(request.getHeader("customer-affiliation-id")).isEqualTo("aff-secret");
        assertThat(request.getHeader("customer-user-id")).isEqualTo("user-secret");
        assertThat(request.getHeader("process-code")).isEqualTo("INTERBANK04");
    }

    @Test
    void should_send_the_same_idempotency_key_when_the_same_item_is_executed_again() throws Exception {
        http.respond(200, "");
        http.respond(200, "");

        StepVerifier.create(client.execute(instruction, CTX, 1)).verifyComplete();
        StepVerifier.create(client.execute(instruction, CTX, 1)).verifyComplete();

        assertThat(http.takeRequest().getHeader("Idempotency-Key"))
                .isEqualTo(http.takeRequest().getHeader("Idempotency-Key"));
    }

    @Test
    void should_translate_an_http_error_into_a_step_failure() {
        http.respond(409, "{}");

        StepVerifier.create(client.execute(instruction, CTX, 0))
                .expectErrorSatisfies(error -> assertThat(error).isInstanceOf(TransferStepFailedException.class)
                        .extracting(e -> ((TransferStepFailedException) e).step()).isEqualTo("execute"))
                .verify();
    }
}
