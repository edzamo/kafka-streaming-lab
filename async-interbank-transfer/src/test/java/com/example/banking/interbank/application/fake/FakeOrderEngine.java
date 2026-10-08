package com.example.banking.interbank.application.fake;

import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.out.OrderEngine;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class FakeOrderEngine implements OrderEngine {

    private final Journal journal;
    private final List<Long> succeeded = new ArrayList<>();
    private final List<Long> failed = new ArrayList<>();
    public boolean failing;
    public RequestContext lastContext;

    public FakeOrderEngine(Journal journal) {
        this.journal = journal;
    }

    @Override
    public Mono<Void> markSucceeded(long orderId, RequestContext ctx) {
        return record("markSucceeded", succeeded, orderId, ctx);
    }

    @Override
    public Mono<Void> markFailed(long orderId, RequestContext ctx) {
        return record("markFailed", failed, orderId, ctx);
    }

    private Mono<Void> record(String step, List<Long> target, long orderId, RequestContext ctx) {
        return Mono.defer(() -> {
            journal.add(step);
            lastContext = ctx;
            if (failing) {
                return Mono.error(new TransferStepFailedException(step, new RuntimeException("boom")));
            }
            target.add(orderId);
            return Mono.empty();
        });
    }

    public List<Long> succeeded() {
        return succeeded;
    }

    public List<Long> failed() {
        return failed;
    }
}
