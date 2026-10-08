package com.example.banking.interbank.adapter.out.http;

import com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.api.InterbankTransferOrderApi;
import com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.invoker.ApiClient;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import com.example.banking.interbank.domain.order.InterbankTransferOrder;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static com.example.banking.interbank.adapter.out.http.HttpFixture.CTX;
import static org.assertj.core.api.Assertions.assertThat;

class TransferOrderQueryClientTest {

    private final HttpFixture http = new HttpFixture();
    private final TransferOrderQueryClient client = new TransferOrderQueryClient(
            new InterbankTransferOrderApi(new ApiClient(http.webClient()).setBasePath(http.baseUrl())), http.calls);

    @AfterEach
    void tearDown() throws Exception {
        http.close();
    }

    @Test
    void should_get_the_order_with_its_items_and_map_it_to_the_domain() throws Exception {
        http.respond(200, """
                {"customerAffiliationId":"aff-9","trayOrderId":"114",
                 "customer":{"fullName":"Jane Doe"},
                 "sourceProduct":{"productNumber":"SRC-1"},
                 "interbankTransferOrderItems":[
                   {"amount":10.50,"currency":{"code":"USD"},"destinationProduct":{"productNumber":"DST-1"}},
                   {"amount":3,"currency":{"code":"EUR"},"destinationProduct":{"productNumber":"DST-2"}}]}
                """);

        StepVerifier.create(client.findWithItems(114L, CTX))
                .expectNext(new InterbankTransferOrder(114L, "aff-9", "SRC-1", "Jane Doe", List.of(
                        new InterbankTransferOrder.Item("DST-1", "USD", new BigDecimal("10.50")),
                        new InterbankTransferOrder.Item("DST-2", "EUR", new BigDecimal("3")))))
                .verifyComplete();

        RecordedRequest request = http.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getRequestUrl().encodedPath()).isEqualTo("/api/v1/interbank-transfer-orders");
        assertThat(request.getRequestUrl().queryParameter("trayOrderId")).isEqualTo("114");
        assertThat(request.getRequestUrl().queryParameter("moreData")).isEqualTo("interbankTransferOrderItems");
        assertThat(request.getHeader("request-id")).isEqualTo("req-1");
        assertThat(request.getHeader("process-code")).isEqualTo("INTERBANK04");
    }

    @Test
    void should_tolerate_an_order_without_items_or_customer() {
        http.respond(200, "{\"customerAffiliationId\":\"aff-9\",\"sourceProduct\":{\"productNumber\":\"SRC-1\"}}");

        StepVerifier.create(client.findWithItems(114L, CTX))
                .expectNext(new InterbankTransferOrder(114L, "aff-9", "SRC-1", null, List.of()))
                .verifyComplete();
    }

    @Test
    void should_translate_an_http_error_into_a_step_failure() {
        http.respond(503, "{}");

        StepVerifier.create(client.findWithItems(114L, CTX))
                .expectErrorSatisfies(error -> assertThat(error).isInstanceOf(TransferStepFailedException.class)
                        .extracting(e -> ((TransferStepFailedException) e).step()).isEqualTo("findWithItems"))
                .verify();
    }
}
