package com.example.banking.interbank.domain.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStatusTest {

    @Test
    void should_require_execution_only_for_final_approval() {
        assertThat(OrderStatus.of("FINAL_APPROVAL").requiresExecution()).isTrue();
        assertThat(OrderStatus.of("PENDING").requiresExecution()).isFalse();
        assertThat(OrderStatus.of("PENDING_SEND").requiresExecution()).isFalse();
    }

    @Test
    void should_require_product_update_for_every_status_but_pending() {
        assertThat(OrderStatus.of("PENDING").requiresProductUpdate()).isFalse();
        assertThat(OrderStatus.of("FINAL_APPROVAL").requiresProductUpdate()).isTrue();
        assertThat(OrderStatus.of("PENDING_SEND").requiresProductUpdate()).isTrue();
    }

    @Test
    void should_keep_the_raw_code_of_a_status_it_does_not_know() {
        assertThat(OrderStatus.of("PENDING_SEND").code()).isEqualTo("PENDING_SEND");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void should_reject_a_missing_status(String code) {
        assertThatThrownBy(() -> OrderStatus.of(code)).isInstanceOf(InvalidOrderStatusException.class);
    }
}
