package com.example.banking.interbank.application.fake;

import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.in.TransferOrderEvent;
import com.example.banking.interbank.application.out.OrderEventPublisher;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class FakeOrderEventPublisher implements OrderEventPublisher {

    public record Republished(TransferOrderEvent event, RequestContext ctx) {
    }

    private final Journal journal;
    private final List<Republished> published = new ArrayList<>();
    public boolean failing;

    public FakeOrderEventPublisher(Journal journal) {
        this.journal = journal;
    }

    @Override
    public Mono<Void> republish(TransferOrderEvent event, RequestContext ctx) {
        return Mono.defer(() -> {
            journal.add("republish");
            if (failing) {
                return Mono.error(new TransferStepFailedException("republish", new RuntimeException("boom")));
            }
            published.add(new Republished(event, ctx));
            return Mono.empty();
        });
    }

    public List<Republished> published() {
        return published;
    }
}
