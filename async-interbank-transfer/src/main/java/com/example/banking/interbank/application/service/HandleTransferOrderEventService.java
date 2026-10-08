package com.example.banking.interbank.application.service;

import com.example.banking.interbank.application.in.HandleTransferOrderEvent;
import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.in.TransferOrderEvent;
import com.example.banking.interbank.application.out.OrderChangeNotifier;
import com.example.banking.interbank.application.out.OrderEngine;
import com.example.banking.interbank.application.out.OrderEventPublisher;
import com.example.banking.interbank.application.out.OrderStatusStore;
import com.example.banking.interbank.application.out.TransferExecutor;
import com.example.banking.interbank.application.out.TransferOrderQuery;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import com.example.banking.interbank.domain.order.OrderStatus;
import com.example.banking.interbank.domain.order.TransferInstruction;
import com.example.banking.interbank.domain.retry.RetryPolicy;
import com.example.banking.interbank.domain.retry.RetrySchedule;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class HandleTransferOrderEventService implements HandleTransferOrderEvent {

    private final OrderStatusStore store;
    private final TransferOrderQuery query;
    private final TransferExecutor executor;
    private final OrderEngine engine;
    private final OrderEventPublisher publisher;
    private final OrderChangeNotifier notifier;
    private final RetryPolicy retryPolicy;
    private final ProcessSettings settings;
    private final Clock clock;
    private final Scheduler scheduler;

    public HandleTransferOrderEventService(OrderStatusStore store, TransferOrderQuery query,
                                           TransferExecutor executor, OrderEngine engine,
                                           OrderEventPublisher publisher, OrderChangeNotifier notifier,
                                           RetryPolicy retryPolicy, ProcessSettings settings,
                                           Clock clock, Scheduler scheduler) {
        this.store = store;
        this.query = query;
        this.executor = executor;
        this.engine = engine;
        this.publisher = publisher;
        this.notifier = notifier;
        this.retryPolicy = retryPolicy;
        this.settings = settings;
        this.clock = clock;
        this.scheduler = scheduler;
    }

    @Override
    public Mono<Void> handle(TransferOrderEvent event, RequestContext ctx) {
        RetrySchedule schedule = RetrySchedule.of(retryPolicy, event.retrySeconds());
        RequestContext enriched = ctx.withProcessCode(processCodeFor(event.nextStatus()))
                .withChannel(event.channel()).withAction(event.action());
        if (schedule.exhausted()) {
            return giveUp(event, enriched);
        }
        return waitFor(schedule)
                .then(Mono.defer(() -> process(event, enriched)))
                .onErrorResume(failure -> publisher.republish(withCounter(event, schedule.escalate()), ctx));
    }

    private static TransferOrderEvent withCounter(TransferOrderEvent event, RetrySchedule schedule) {
        return new TransferOrderEvent(event.orderId(), event.customerUserId(), event.eventDate(), event.status(),
                event.nextStatus(), event.channel(), event.action(), schedule.current());
    }

    private String processCodeFor(OrderStatus status) {
        return status.requiresExecution() ? settings.executeProcessCode() : settings.modifyProcessCode();
    }

    private Mono<Void> waitFor(RetrySchedule schedule) {
        if (schedule.current() == 0) {
            return Mono.empty();
        }
        return Mono.delay(Duration.ofSeconds(schedule.current()), scheduler).then();
    }

    private Mono<Void> giveUp(TransferOrderEvent event, RequestContext ctx) {
        return store.updateStatus(event.orderId(), settings.errorStatus(), OffsetDateTime.now(clock), ctx)
                .then(engine.markFailed(event.orderId(), ctx))
                .then(notifyBestEffort(event.orderId()))
                .onErrorResume(failure -> Mono.empty());
    }

    private Mono<Void> notifyBestEffort(long orderId) {
        return notifier.notifyChanged(orderId).onErrorResume(failure -> Mono.empty());
    }

    private Mono<Void> process(TransferOrderEvent event, RequestContext ctx) {
        Mono<Void> productUpdate = event.nextStatus().requiresProductUpdate()
                ? updateProduct(event, ctx) : Mono.empty();
        Mono<Void> execution = event.nextStatus().requiresExecution()
                ? execute(event, ctx) : Mono.empty();
        return productUpdate.then(execution);
    }

    private Mono<Void> updateProduct(TransferOrderEvent event, RequestContext ctx) {
        return store.updateStatus(event.orderId(), event.nextStatus().code(), null, ctx)
                .then(notifyBestEffort(event.orderId()));
    }

    private Mono<Void> execute(TransferOrderEvent event, RequestContext ctx) {
        return query.findWithItems(event.orderId(), ctx)
                .switchIfEmpty(Mono.error(() -> new TransferStepFailedException("findWithItems",
                        new IllegalStateException("order not found"))))
                .flatMap(order -> {
                    List<TransferInstruction> instructions = order.instructions();
                    RequestContext orderCtx = ctx.withCustomerAffiliationId(order.customerAffiliationId());
                    return Flux.range(0, instructions.size())
                            .concatMap(index -> executor.execute(instructions.get(index), orderCtx, index))
                            .then();
                })
                .then(engine.markSucceeded(event.orderId(), ctx))
                .then(notifyBestEffort(event.orderId()));
    }
}
