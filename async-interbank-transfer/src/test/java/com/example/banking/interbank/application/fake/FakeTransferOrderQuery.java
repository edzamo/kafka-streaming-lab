package com.example.banking.interbank.application.fake;

import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.out.TransferOrderQuery;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import com.example.banking.interbank.domain.order.InterbankTransferOrder;
import reactor.core.publisher.Mono;

public class FakeTransferOrderQuery implements TransferOrderQuery {

    private final Journal journal;
    public InterbankTransferOrder order;
    public boolean failing;
    public RequestContext lastContext;

    public FakeTransferOrderQuery(Journal journal) {
        this.journal = journal;
    }

    @Override
    public Mono<InterbankTransferOrder> findWithItems(long orderId, RequestContext ctx) {
        return Mono.defer(() -> {
            journal.add("findWithItems");
            lastContext = ctx;
            if (failing) {
                return Mono.error(new TransferStepFailedException("findWithItems", new RuntimeException("boom")));
            }
            return Mono.justOrEmpty(order);
        });
    }
}
