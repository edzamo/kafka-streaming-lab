package com.example.banking.interbank.application.out;

import com.example.banking.interbank.application.in.RequestContext;
import reactor.core.publisher.Mono;

public interface OrderEngine {

    Mono<Void> markSucceeded(long orderId, RequestContext ctx);

    Mono<Void> markFailed(long orderId, RequestContext ctx);
}
