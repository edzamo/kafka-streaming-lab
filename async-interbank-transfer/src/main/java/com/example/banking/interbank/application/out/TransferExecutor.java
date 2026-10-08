package com.example.banking.interbank.application.out;

import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.domain.order.TransferInstruction;
import reactor.core.publisher.Mono;

public interface TransferExecutor {

    /** Executes one item; the (requestId, itemIndex) pair is stable across retries so the callee can deduplicate. */
    Mono<Void> execute(TransferInstruction instruction, RequestContext ctx, int itemIndex);
}
