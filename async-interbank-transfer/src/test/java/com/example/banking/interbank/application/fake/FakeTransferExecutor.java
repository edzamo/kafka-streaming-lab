package com.example.banking.interbank.application.fake;

import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.out.TransferExecutor;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import com.example.banking.interbank.domain.order.TransferInstruction;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class FakeTransferExecutor implements TransferExecutor {

    public record Execution(TransferInstruction instruction, RequestContext ctx, int itemIndex) {
    }

    private final Journal journal;
    private final List<Execution> executions = new ArrayList<>();
    /** Item index that fails (once executed it is still recorded as attempted); -1 for none. */
    public int failAtIndex = -1;

    public FakeTransferExecutor(Journal journal) {
        this.journal = journal;
    }

    @Override
    public Mono<Void> execute(TransferInstruction instruction, RequestContext ctx, int itemIndex) {
        return Mono.defer(() -> {
            journal.add("execute:" + itemIndex);
            executions.add(new Execution(instruction, ctx, itemIndex));
            if (itemIndex == failAtIndex) {
                return Mono.error(new TransferStepFailedException("execute", new RuntimeException("boom")));
            }
            return Mono.empty();
        });
    }

    public List<Execution> executions() {
        return executions;
    }
}
