package com.example.banking.interbank.application.fake;

import com.example.banking.interbank.application.out.OrderChangeNotifier;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class FakeOrderChangeNotifier implements OrderChangeNotifier {

    private final Journal journal;
    private final List<Long> notified = new ArrayList<>();
    public boolean failing;

    public FakeOrderChangeNotifier(Journal journal) {
        this.journal = journal;
    }

    @Override
    public Mono<Void> notifyChanged(long orderId) {
        return Mono.defer(() -> {
            journal.add("notify");
            if (failing) {
                return Mono.error(new TransferStepFailedException("notify", new RuntimeException("boom")));
            }
            notified.add(orderId);
            return Mono.empty();
        });
    }

    public List<Long> notified() {
        return notified;
    }
}
