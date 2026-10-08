package com.example.banking.interbank.application.out;

import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.domain.order.InterbankTransferOrder;
import reactor.core.publisher.Mono;

public interface TransferOrderQuery {

    Mono<InterbankTransferOrder> findWithItems(long orderId, RequestContext ctx);
}
