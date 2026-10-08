package com.example.banking.interbank.adapter.out.messaging;

import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.in.TransferOrderEvent;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import com.example.banking.interbank.domain.order.OrderStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StreamBridgeOrderEventPublisherTest {

    private final StreamBridge bridge = mock(StreamBridge.class);
    private final StreamBridgeOrderEventPublisher publisher =
            new StreamBridgeOrderEventPublisher(bridge, "retry-out-0");
    private final TransferOrderEvent event = new TransferOrderEvent(114L, "user-1", "2022-03-26T00:00:00",
            "PENDING", OrderStatus.of("FINAL_APPROVAL"), "web", "APPROVE", 60);
    private final RequestContext ctx = new RequestContext("req-1", "INTERBANK04", "Banking", "session-1", "EN",
            "aff-1", "user-1", "web", "APPROVE");

    @SuppressWarnings("unchecked")
    private Message<String> sentMessage() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(bridge).send(eq("retry-out-0"), captor.capture());
        return (Message<String>) captor.getValue();
    }

    @Test
    void should_send_the_event_as_json_with_the_counter_to_the_configured_binding() throws Exception {
        when(bridge.send(any(String.class), any(Object.class))).thenReturn(true);

        StepVerifier.create(publisher.republish(event, ctx)).verifyComplete();

        JsonNode json = new ObjectMapper().readTree(sentMessage().getPayload());
        assertThat(json.path("orderId").asLong()).isEqualTo(114L);
        assertThat(json.path("customerUserId").asText()).isEqualTo("user-1");
        assertThat(json.path("eventDate").asText()).isEqualTo("2022-03-26T00:00:00");
        assertThat(json.path("status").asText()).isEqualTo("PENDING");
        assertThat(json.path("nextStatus").asText()).isEqualTo("FINAL_APPROVAL");
        assertThat(json.path("channel").asText()).isEqualTo("web");
        assertThat(json.path("action").asText()).isEqualTo("APPROVE");
        assertThat(json.path("retrySeconds").asInt()).isEqualTo(60);
    }

    @Test
    void should_forward_the_request_id_and_the_rest_of_the_context_as_message_headers() {
        when(bridge.send(any(String.class), any(Object.class))).thenReturn(true);

        StepVerifier.create(publisher.republish(event, ctx)).verifyComplete();

        var headers = sentMessage().getHeaders();
        assertThat(headers.get("request-id")).isEqualTo("req-1");
        assertThat(headers.get("app-name")).isEqualTo("Banking");
        assertThat(headers.get("x-session")).isEqualTo("session-1");
        assertThat(headers.get("x-language")).isEqualTo("EN");
        assertThat(headers.get("customer-affiliation-id")).isEqualTo("aff-1");
        assertThat(headers.get("customer-user-id")).isEqualTo("user-1");
    }

    @Test
    void should_fail_the_step_when_the_bridge_reports_the_send_was_not_accepted() {
        when(bridge.send(any(String.class), any(Object.class))).thenReturn(false);

        StepVerifier.create(publisher.republish(event, ctx))
                .expectErrorSatisfies(error -> assertThat(error).isInstanceOf(TransferStepFailedException.class)
                        .extracting(e -> ((TransferStepFailedException) e).step()).isEqualTo("republish"))
                .verify();
    }

    @Test
    void should_fail_the_step_when_the_bridge_throws() {
        when(bridge.send(any(String.class), any(Object.class))).thenThrow(new IllegalStateException("broker down"));

        StepVerifier.create(publisher.republish(event, ctx))
                .expectError(TransferStepFailedException.class).verify();
    }
}
