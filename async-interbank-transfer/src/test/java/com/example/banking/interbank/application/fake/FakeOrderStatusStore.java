package com.example.banking.interbank.application.fake;

import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.out.OrderStatusStore;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class FakeOrderStatusStore implements OrderStatusStore {

    public record Update(long orderId, String status, OffsetDateTime executionDate, RequestContext ctx) {
    }

    private final Journal journal;
    private final List<Update> updates = new ArrayList<>();
    public boolean failing;

    public FakeOrderStatusStore(Journal journal) {
        this.journal = journal;
    }

    @Override
    public Mono<Void> updateStatus(long orderId, String status, OffsetDateTime executionDate, RequestContext ctx) {
        return Mono.defer(() -> {
            journal.add("updateStatus:" + status);
            if (failing) {
                return Mono.error(new TransferStepFailedException("updateStatus", new RuntimeException("boom")));
            }
            updates.add(new Update(orderId, status, executionDate, ctx));
            return Mono.empty();
        });
    }

    public List<Update> updates() {
        return updates;
    }
}
