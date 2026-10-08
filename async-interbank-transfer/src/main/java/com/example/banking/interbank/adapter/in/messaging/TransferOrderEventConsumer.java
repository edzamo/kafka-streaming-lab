package com.example.banking.interbank.adapter.in.messaging;

import com.example.banking.interbank.application.in.HandleTransferOrderEvent;
import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.in.TransferOrderEvent;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import com.example.banking.interbank.domain.order.OrderStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import reactor.util.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Spring Cloud Stream function {@code receive}. Reads the message, hands it to the use case and subscribes once.
 * Unreadable or incomplete messages are discarded (logged, never retried). Nothing from the payload or the
 * headers is logged.
 */
@Component("receive")
class TransferOrderEventConsumer implements Consumer<Message<String>> {

    private static final Logger log = LoggerFactory.getLogger(TransferOrderEventConsumer.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final HandleTransferOrderEvent handleEvent;

    TransferOrderEventConsumer(HandleTransferOrderEvent handleEvent) {
        this.handleEvent = handleEvent;
    }

    @Override
    public void accept(Message<String> message) {
        TransferOrderEventMessage parsed = parse(message.getPayload());
        if (parsed == null) {
            return;
        }
        if (parsed.orderId() == null || parsed.nextStatus() == null || parsed.nextStatus().isBlank()) {
            log.warn("Discarding message without order id or next status");
            return;
        }
        TransferOrderEvent event = new TransferOrderEvent(parsed.orderId(), parsed.customerUserId(),
                parsed.eventDate(), parsed.status(), OrderStatus.of(parsed.nextStatus()), parsed.channel(),
                parsed.action(), parsed.retrySeconds());
        RequestContext ctx = contextFrom(message.getHeaders(), event);
        log.info("Processing order {}, action {}", event.orderId(), event.action());
        handleEvent.handle(event, ctx)
                .contextWrite(Context.of("MDC_CTX", Map.of("request-id", ctx.requestId())))
                .subscribe(unused -> { },
                        failure -> log.error("Handling order {} failed: {}", event.orderId(), describe(failure)));
    }

    private static TransferOrderEventMessage parse(String payload) {
        try {
            TransferOrderEventMessage parsed = JSON.readValue(payload, TransferOrderEventMessage.class);
            if (parsed == null) {
                log.warn("Discarding message with an empty payload");
            }
            return parsed;
        } catch (JsonProcessingException | IllegalArgumentException failure) {
            log.warn("Discarding message whose payload is not a readable order event");
            return null;
        }
    }

    private static RequestContext contextFrom(MessageHeaders headers, TransferOrderEvent event) {
        String requestId = headerValue(headers, "request-id");
        return new RequestContext(
                requestId != null ? requestId : "def-" + UUID.randomUUID(),
                null,
                headerValue(headers, "app-name"),
                headerValue(headers, "x-session"),
                headerValue(headers, "x-language"),
                headerValue(headers, "customer-affiliation-id"),
                headerValue(headers, "customer-user-id"),
                event.channel(),
                event.action());
    }

    /** Kafka headers written by other producers arrive as bytes; the ones this service writes as strings. */
    private static String headerValue(MessageHeaders headers, String name) {
        Object value = headers.get(name);
        if (value instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return value == null ? null : value.toString();
    }

    private static String describe(Throwable failure) {
        if (failure instanceof TransferStepFailedException step) {
            return "step " + step.step();
        }
        return failure.getClass().getSimpleName();
    }
}
