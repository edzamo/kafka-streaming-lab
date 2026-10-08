package com.example.banking.interbank.adapter.out.http;

import com.example.banking.interbank.adapter.out.http.generated.bsOrder.api.OrderApi;
import com.example.banking.interbank.adapter.out.http.generated.bsOrder.invoker.ApiClient;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static com.example.banking.interbank.adapter.out.http.HttpFixture.CTX;
import static org.assertj.core.api.Assertions.assertThat;

class OrderEngineClientTest {

    private final HttpFixture http = new HttpFixture();
    private final OrderEngineClient client = new OrderEngineClient(
            new OrderApi(new ApiClient(http.webClient()).setBasePath(http.baseUrl())), http.calls, "system-user");

    @AfterEach
    void tearDown() throws Exception {
        http.close();
    }

    @Test
    void should_patch_the_order_with_the_success_action_for_the_system_user() throws Exception {
        http.respond(200, "");

        StepVerifier.create(client.markSucceeded(114L, CTX)).verifyComplete();

        RecordedRequest request = http.takeRequest();
        assertThat(request.getMethod()).isEqualTo("PATCH");
        assertThat(request.getPath()).isEqualTo("/api/v1/orders/114");
        assertThat(request.getHeader("channel")).isEqualTo("web");
        var body = HttpFixture.json(request);
        assertThat(body.path("customerUser").path("customerUserId").asText()).isEqualTo("system-user");
        assertThat(body.path("action").path("code").asText()).isEqualTo("TO_SUCCESS");
    }

    @Test
    void should_patch_the_order_with_the_fail_action_when_marking_it_failed() throws Exception {
        http.respond(200, "");

        StepVerifier.create(client.markFailed(114L, CTX)).verifyComplete();

        assertThat(HttpFixture.json(http.takeRequest()).path("action").path("code").asText()).isEqualTo("TO_FAIL");
    }

    @Test
    void should_forward_the_traceability_headers_the_contract_does_not_declare() throws Exception {
        http.respond(200, "");

        StepVerifier.create(client.markSucceeded(114L, CTX)).verifyComplete();

        RecordedRequest request = http.takeRequest();
        assertThat(request.getHeader("request-id")).isEqualTo("req-1");
        assertThat(request.getHeader("process-code")).isEqualTo("INTERBANK04");
        assertThat(request.getHeader("app-name")).isEqualTo("Banking");
        assertThat(request.getHeader("caller-name")).isEqualTo("async-interbank-transfer");
        assertThat(request.getHeader("customer-user-id")).isEqualTo("user-secret");
        assertThat(request.getHeader("x-session")).isEqualTo("session-secret");
    }

    @Test
    void should_translate_an_http_error_into_a_step_failure() {
        http.respond(500, "{}");

        StepVerifier.create(client.markSucceeded(114L, CTX))
                .expectErrorSatisfies(error -> assertThat(error).isInstanceOf(TransferStepFailedException.class)
                        .extracting(e -> ((TransferStepFailedException) e).step()).isEqualTo("markSucceeded"))
                .verify();
    }
}
