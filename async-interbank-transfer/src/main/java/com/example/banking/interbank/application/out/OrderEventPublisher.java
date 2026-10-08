package com.example.banking.interbank.application.out;

import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.in.TransferOrderEvent;
import reactor.core.publisher.Mono;

public interface OrderEventPublisher {

    /** Publishes the event again; the request id of {@code ctx} must travel with it. */
    Mono<Void> republish(TransferOrderEvent event, RequestContext ctx);
}
