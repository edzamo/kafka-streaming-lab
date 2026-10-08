package com.example.banking.interbank.application.service;

import com.example.banking.interbank.application.fake.FakeOrderChangeNotifier;
import com.example.banking.interbank.application.fake.FakeOrderEngine;
import com.example.banking.interbank.application.fake.FakeOrderEventPublisher;
import com.example.banking.interbank.application.fake.FakeOrderStatusStore;
import com.example.banking.interbank.application.fake.FakeTransferExecutor;
import com.example.banking.interbank.application.fake.FakeTransferOrderQuery;
import com.example.banking.interbank.application.fake.Journal;
import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.in.TransferOrderEvent;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import com.example.banking.interbank.domain.order.OrderStatus;
import com.example.banking.interbank.domain.retry.RetryPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import com.example.banking.interbank.domain.order.InterbankTransferOrder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class HandleTransferOrderEventServiceTest {

    static final long ORDER_ID = 114L;

    final Journal journal = new Journal();
    final FakeOrderStatusStore store = new FakeOrderStatusStore(journal);
    final FakeTransferOrderQuery query = new FakeTransferOrderQuery(journal);
    final FakeTransferExecutor executor = new FakeTransferExecutor(journal);
    final FakeOrderEngine engine = new FakeOrderEngine(journal);
    final FakeOrderEventPublisher publisher = new FakeOrderEventPublisher(journal);
    final FakeOrderChangeNotifier notifier = new FakeOrderChangeNotifier(journal);
    final VirtualTimeScheduler scheduler = VirtualTimeScheduler.create();
    final Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
    final RequestContext ctx = new RequestContext("req-1", null, "Banking", "session-1", "ES",
            "aff-1", "user-1", null, null);

    HandleTransferOrderEventService service;

    @BeforeEach
    void setUp() {
        service = new HandleTransferOrderEventService(store, query, executor, engine, publisher, notifier,
                new RetryPolicy(0, 30, 120), new ProcessSettings("INTERBANK04", "INTERBANK02", "ERROR"),
                clock, scheduler);
    }

    static TransferOrderEvent event(String nextStatus, Integer retrySeconds) {
        return new TransferOrderEvent(ORDER_ID, "user-1", "2022-03-26T00:00:00", "PENDING",
                OrderStatus.of(nextStatus), "web", "APPROVE", retrySeconds);
    }

    private StepVerifier.FirstStep<Void> handling(TransferOrderEvent event) {
        return handling(event, ctx);
    }

    private StepVerifier.FirstStep<Void> handling(TransferOrderEvent event, RequestContext context) {
        return StepVerifier.withVirtualTime(() -> service.handle(event, context), () -> scheduler, Long.MAX_VALUE);
    }

    static InterbankTransferOrder orderWithTwoItems() {
        return new InterbankTransferOrder(ORDER_ID, "order-aff", "SRC-1", "Jane Doe", java.util.List.of(
                new InterbankTransferOrder.Item("DST-1", "USD", new BigDecimal("10")),
                new InterbankTransferOrder.Item("DST-2", "USD", new BigDecimal("20"))));
    }

    @Test
    void should_do_nothing_when_the_next_status_is_pending() {
        handling(event("PENDING", null))
                .verifyComplete();

        assertThat(journal.entries()).isEmpty();
    }

    @Test
    void should_update_the_product_status_and_notify_when_the_status_is_not_pending_nor_final() {
        handling(event("PENDING_SEND", null))
                .verifyComplete();

        assertThat(journal.entries()).containsExactly("updateStatus:PENDING_SEND", "notify");
        assertThat(store.updates()).singleElement().satisfies(update -> {
            assertThat(update.orderId()).isEqualTo(ORDER_ID);
            assertThat(update.executionDate()).isNull();
        });
        assertThat(notifier.notified()).containsExactly(ORDER_ID);
    }

    @Test
    void should_wait_the_counter_seconds_on_the_scheduler_before_doing_anything() {
        handling(event("PENDING_SEND", 30))
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(29))
                .then(() -> assertThat(journal.entries()).isEmpty())
                .thenAwait(Duration.ofSeconds(1))
                .verifyComplete();

        assertThat(journal.entries()).containsExactly("updateStatus:PENDING_SEND", "notify");
    }

    @Test
    void should_execute_every_item_in_order_and_mark_the_order_succeeded_on_final_approval() {
        query.order = orderWithTwoItems();

        handling(event("FINAL_APPROVAL", null))
                .verifyComplete();

        assertThat(journal.entries()).containsExactly("updateStatus:FINAL_APPROVAL", "notify", "findWithItems",
                "execute:0", "execute:1", "markSucceeded", "notify");
        assertThat(executor.executions()).extracting(FakeTransferExecutor.Execution::instruction)
                .containsExactlyElementsOf(orderWithTwoItems().instructions());
        assertThat(engine.succeeded()).containsExactly(ORDER_ID);
        assertThat(publisher.published()).isEmpty();
    }

    @Test
    void should_choose_the_modify_process_code_for_status_changes() {
        handling(event("PENDING_SEND", null))
                .verifyComplete();

        assertThat(store.updates()).singleElement()
                .satisfies(update -> assertThat(update.ctx().processCode()).isEqualTo("INTERBANK02"));
    }

    @Test
    void should_choose_the_execute_process_code_and_enrich_the_context_for_final_approval() {
        query.order = orderWithTwoItems();

        handling(event("FINAL_APPROVAL", null))
                .verifyComplete();

        assertThat(executor.executions()).hasSize(2).allSatisfy(execution -> {
            assertThat(execution.ctx().processCode()).isEqualTo("INTERBANK04");
            assertThat(execution.ctx().requestId()).isEqualTo("req-1");
            assertThat(execution.ctx().customerAffiliationId()).isEqualTo("order-aff");
            assertThat(execution.ctx().channel()).isEqualTo("web");
            assertThat(execution.ctx().action()).isEqualTo("APPROVE");
        });
        assertThat(engine.lastContext.processCode()).isEqualTo("INTERBANK04");
        assertThat(engine.lastContext.channel()).isEqualTo("web");
        assertThat(engine.lastContext.customerAffiliationId()).isEqualTo("aff-1");
    }

    enum FailingStep { STORE, QUERY, FIRST_ITEM, SECOND_ITEM, MARK_SUCCEEDED }

    private void breakStep(FailingStep step) {
        switch (step) {
            case STORE -> store.failing = true;
            case QUERY -> query.failing = true;
            case FIRST_ITEM -> executor.failAtIndex = 0;
            case SECOND_ITEM -> executor.failAtIndex = 1;
            case MARK_SUCCEEDED -> engine.failing = true;
        }
    }

    @ParameterizedTest
    @EnumSource(FailingStep.class)
    void should_republish_with_an_escalated_counter_and_complete_when_a_step_fails(FailingStep step) {
        query.order = orderWithTwoItems();
        breakStep(step);

        handling(event("FINAL_APPROVAL", null))
                .verifyComplete();

        assertThat(publisher.published()).singleElement().satisfies(republished -> {
            assertThat(republished.event().retrySeconds()).isEqualTo(30);
            assertThat(republished.event().orderId()).isEqualTo(ORDER_ID);
            assertThat(republished.event().nextStatus()).isEqualTo(OrderStatus.of("FINAL_APPROVAL"));
            assertThat(republished.ctx()).isEqualTo(ctx);
        });
        assertThat(engine.succeeded()).isEmpty();
        assertThat(journal.entries()).last().isEqualTo("republish");
    }

    @Test
    void should_not_go_past_the_failed_item_when_republishing() {
        query.order = orderWithTwoItems();
        executor.failAtIndex = 0;

        handling(event("FINAL_APPROVAL", null))
                .verifyComplete();

        assertThat(journal.entries()).doesNotContain("execute:1", "markSucceeded");
    }

    @Test
    void should_escalate_from_the_counter_the_event_already_carries() {
        store.failing = true;

        handling(event("PENDING_SEND", 60))
                .thenAwait(Duration.ofSeconds(60))
                .verifyComplete();

        assertThat(publisher.published()).singleElement()
                .satisfies(republished -> assertThat(republished.event().retrySeconds()).isEqualTo(90));
    }

    // Characterization: pinned after the first retry implementation, not a RED cycle (see report).
    @Test
    void should_propagate_the_error_when_the_republish_itself_fails() {
        store.failing = true;
        publisher.failing = true;

        handling(event("PENDING_SEND", null))
                .expectError(TransferStepFailedException.class)
                .verify(Duration.ofSeconds(2));
    }

    // Characterization
    @Test
    void should_mark_succeeded_without_executing_anything_when_the_order_has_no_items() {
        query.order = new InterbankTransferOrder(ORDER_ID, "order-aff", "SRC-1", "Jane Doe", java.util.List.of());

        handling(event("FINAL_APPROVAL", null))
                .verifyComplete();

        assertThat(executor.executions()).isEmpty();
        assertThat(engine.succeeded()).containsExactly(ORDER_ID);
    }

    // Characterization
    @Test
    void should_republish_without_executing_when_an_item_is_invalid() {
        query.order = new InterbankTransferOrder(ORDER_ID, "order-aff", "SRC-1", "Jane Doe", java.util.List.of(
                new InterbankTransferOrder.Item("DST-1", "USD", BigDecimal.ZERO)));

        handling(event("FINAL_APPROVAL", null))
                .verifyComplete();

        assertThat(executor.executions()).isEmpty();
        assertThat(engine.succeeded()).isEmpty();
        assertThat(publisher.published()).hasSize(1);
    }

    @Test
    void should_not_break_the_flow_when_the_notifier_fails() {
        query.order = orderWithTwoItems();
        notifier.failing = true;

        handling(event("FINAL_APPROVAL", null))
                .verifyComplete();

        assertThat(executor.executions()).hasSize(2);
        assertThat(engine.succeeded()).containsExactly(ORDER_ID);
        assertThat(publisher.published()).isEmpty();
    }

    @Test
    void should_mark_the_order_failed_with_the_error_status_and_skip_the_wait_when_the_counter_is_exhausted() {
        query.order = orderWithTwoItems();

        handling(event("FINAL_APPROVAL", 120))
                .expectComplete()
                .verify(Duration.ofSeconds(2));

        assertThat(journal.entries()).containsExactly("updateStatus:ERROR", "markFailed", "notify");
        assertThat(store.updates()).singleElement().satisfies(update ->
                assertThat(update.executionDate()).isEqualTo(OffsetDateTime.parse("2026-01-01T10:00:00Z")));
        assertThat(engine.failed()).containsExactly(ORDER_ID);
        assertThat(publisher.published()).isEmpty();
    }

    @Test
    void should_complete_without_error_when_closing_an_exhausted_order_fails() {
        store.failing = true;

        handling(event("FINAL_APPROVAL", 150))
                .expectComplete()
                .verify(Duration.ofSeconds(2));

        assertThat(publisher.published()).isEmpty();
    }

    // Characterization of the retry cycle (use case level): the republished event and context, handled again,
    // reach the executor with the same (requestId, itemIndex) pairs, so the callee can deduplicate.
    @Test
    void should_execute_the_same_request_id_and_item_indexes_again_when_the_republished_event_is_handled() {
        query.order = orderWithTwoItems();
        engine.failing = true;
        // no wait on the first attempt, so no virtual time: the scheduler is reused by the second attempt
        StepVerifier.create(service.handle(event("FINAL_APPROVAL", null), ctx)).verifyComplete();
        var republished = publisher.published().get(0);
        var firstAttempt = executor.executions().stream()
                .map(e -> e.ctx().requestId() + "-" + e.itemIndex()).toList();
        engine.failing = false;

        handling(republished.event(), republished.ctx())
                .thenAwait(Duration.ofSeconds(30))
                .verifyComplete();

        var secondAttempt = executor.executions().stream().skip(firstAttempt.size())
                .map(e -> e.ctx().requestId() + "-" + e.itemIndex()).toList();
        assertThat(firstAttempt).containsExactly("req-1-0", "req-1-1");
        assertThat(secondAttempt).isEqualTo(firstAttempt);
        assertThat(engine.succeeded()).containsExactly(ORDER_ID);
    }

    @Test
    void should_republish_instead_of_marking_succeeded_when_the_order_cannot_be_found() {
        query.order = null;

        handling(event("FINAL_APPROVAL", null))
                .verifyComplete();

        assertThat(engine.succeeded()).isEmpty();
        assertThat(publisher.published()).hasSize(1);
    }
}
