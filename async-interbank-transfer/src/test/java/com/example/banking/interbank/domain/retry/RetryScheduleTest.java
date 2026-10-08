package com.example.banking.interbank.domain.retry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryScheduleTest {

    private final RetryPolicy policy = new RetryPolicy(0, 30, 120);

    @Test
    void should_start_at_initial_seconds_when_event_carries_no_counter() {
        RetrySchedule schedule = RetrySchedule.of(policy, null);

        assertThat(schedule.current()).isEqualTo(0);
    }

    @Test
    void should_keep_the_counter_carried_by_the_event_when_present() {
        RetrySchedule schedule = RetrySchedule.of(policy, 60);

        assertThat(schedule.current()).isEqualTo(60);
    }

    @Test
    void should_grow_in_fixed_steps_when_escalated() {
        RetrySchedule next = RetrySchedule.of(policy, 30).escalate();

        assertThat(next.current()).isEqualTo(60);
        assertThat(next.escalate().current()).isEqualTo(90);
    }

    @ParameterizedTest
    @CsvSource({"0,false", "90,false", "119,false", "120,true", "150,true"})
    void should_be_exhausted_when_counter_reaches_the_limit(int current, boolean exhausted) {
        assertThat(RetrySchedule.of(policy, current).exhausted()).isEqualTo(exhausted);
    }

    @ParameterizedTest
    @CsvSource({"0,0,120", "-1,30,120", "0,30,-5"})
    void should_reject_a_policy_that_can_never_exhaust_or_is_negative(int initial, int step, int limit) {
        assertThatThrownBy(() -> new RetryPolicy(initial, step, limit))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
