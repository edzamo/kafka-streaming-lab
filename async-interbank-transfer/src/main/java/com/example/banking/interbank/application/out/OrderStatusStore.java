package com.example.banking.interbank.application.out;

import com.example.banking.interbank.application.in.RequestContext;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public interface OrderStatusStore {

    Mono<Void> updateStatus(long orderId, String status, OffsetDateTime executionDate, RequestContext ctx);
}
