package com.example.banking.interbank.domain.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterbankTransferOrderTest {

    @Test
    void should_yield_no_instructions_when_the_order_has_no_items() {
        var order = new InterbankTransferOrder(7L, "aff-1", "SRC-1", "Jane Doe", List.of());

        assertThat(order.instructions()).isEmpty();
    }

    @Test
    void should_build_one_instruction_per_item_in_item_order() {
        var order = new InterbankTransferOrder(7L, "aff-1", "SRC-1", "Jane Doe", List.of(
                new InterbankTransferOrder.Item("DST-1", "USD", new BigDecimal("10.50")),
                new InterbankTransferOrder.Item("DST-2", "EUR", new BigDecimal("3"))));

        assertThat(order.instructions()).containsExactly(
                new TransferInstruction(7L, "SRC-1", "DST-1", "USD", new BigDecimal("10.50"), "Jane Doe"),
                new TransferInstruction(7L, "SRC-1", "DST-2", "EUR", new BigDecimal("3"), "Jane Doe"));
    }

    static java.util.stream.Stream<InterbankTransferOrder> invalidOrders() {
        var ok = new BigDecimal("10");
        return java.util.stream.Stream.of(
                orderOf("SRC-1", new InterbankTransferOrder.Item("DST-1", null, ok)),
                orderOf("SRC-1", new InterbankTransferOrder.Item("DST-1", " ", ok)),
                orderOf("SRC-1", new InterbankTransferOrder.Item("DST-1", "USD", null)),
                orderOf("SRC-1", new InterbankTransferOrder.Item("DST-1", "USD", BigDecimal.ZERO)),
                orderOf("SRC-1", new InterbankTransferOrder.Item("DST-1", "USD", new BigDecimal("-1"))),
                orderOf("SRC-1", new InterbankTransferOrder.Item(null, "USD", ok)),
                orderOf("SRC-1", new InterbankTransferOrder.Item("", "USD", ok)),
                orderOf(null, new InterbankTransferOrder.Item("DST-1", "USD", ok)),
                orderOf(" ", new InterbankTransferOrder.Item("DST-1", "USD", ok)));
    }

    private static InterbankTransferOrder orderOf(String source, InterbankTransferOrder.Item item) {
        return new InterbankTransferOrder(7L, "aff-1", source, "Jane Doe", List.of(item));
    }

    @ParameterizedTest
    @MethodSource("invalidOrders")
    void should_reject_an_item_without_currency_positive_amount_or_products(InterbankTransferOrder order) {
        assertThatThrownBy(order::instructions).isInstanceOf(InvalidTransferItemException.class);
    }

    @Test
    void should_not_expose_sensitive_data_in_toString() {
        var order = new InterbankTransferOrder(7L, "aff-secret", "SRC-secret", "Jane Doe", List.of(
                new InterbankTransferOrder.Item("DST-secret", "USD", new BigDecimal("98765.43"))));

        String rendered = order + " " + order.items().get(0) + " " + order.instructions().get(0);

        assertThat(rendered).doesNotContain("aff-secret", "SRC-secret", "DST-secret", "Jane", "98765");
    }

    @Test
    void should_treat_a_missing_item_list_as_an_order_without_items() {
        var order = new InterbankTransferOrder(7L, "aff-1", "SRC-1", "Jane Doe", null);

        assertThat(order.instructions()).isEmpty();
    }
}
