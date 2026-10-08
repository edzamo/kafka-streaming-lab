package com.example.banking.interbank.application.in;

import reactor.core.publisher.Mono;

public interface HandleTransferOrderEvent {

    /** Completes without error unless the event could not be republished for retry. */
    Mono<Void> handle(TransferOrderEvent event, RequestContext ctx);
}
