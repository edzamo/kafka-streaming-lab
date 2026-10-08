package com.example.banking.interbank.adapter.out.messaging;

import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.in.TransferOrderEvent;
import com.example.banking.interbank.application.out.OrderEventPublisher;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class StreamBridgeOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(StreamBridgeOrderEventPublisher.class);
    private static final String STEP = "republish";
    private static final ObjectMapper JSON = new ObjectMapper();

    /** Wire format of a republished event; the consumer reads the same field names. */
    private record Payload(long orderId, String customerUserId, String eventDate, String status,
                           String nextStatus, String channel, String action, Integer retrySeconds) {
    }

    private final StreamBridge streamBridge;
    private final String binding;

    public StreamBridgeOrderEventPublisher(StreamBridge streamBridge, String binding) {
        this.streamBridge = streamBridge;
        this.binding = binding;
    }

    @Override
    public Mono<Void> republish(TransferOrderEvent event, RequestContext ctx) {
        return Mono.fromCallable(() -> send(event, ctx))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(failure -> {
                    log.warn("Republishing order {} failed: {}", event.orderId(), failure.getClass().getSimpleName());
                    return new TransferStepFailedException(STEP, failure);
                })
                .then();
    }

    private Boolean send(TransferOrderEvent event, RequestContext ctx) throws JsonProcessingException {
        Message<String> message = messageFor(event, ctx);
        if (!streamBridge.send(binding, message)) {
            throw new IllegalStateException("The bridge did not accept the message");
        }
        return Boolean.TRUE;
    }

    private static Message<String> messageFor(TransferOrderEvent event, RequestContext ctx)
            throws JsonProcessingException {
        Payload payload = new Payload(event.orderId(), event.customerUserId(), event.eventDate(), event.status(),
                event.nextStatus().code(), event.channel(), event.action(), event.retrySeconds());
        MessageBuilder<String> builder = MessageBuilder.withPayload(JSON.writeValueAsString(payload));
        header(builder, "app-name", ctx.appName());
        header(builder, "request-id", ctx.requestId());
        header(builder, "x-session", ctx.session());
        header(builder, "x-language", ctx.language());
        header(builder, "customer-affiliation-id", ctx.customerAffiliationId());
        header(builder, "customer-user-id", ctx.customerUserId());
        return builder.build();
    }

    private static void header(MessageBuilder<String> builder, String name, String value) {
        if (value != null) {
            builder.setHeader(name, value);
        }
    }
}
