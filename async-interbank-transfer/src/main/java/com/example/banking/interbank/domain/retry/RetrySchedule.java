package com.example.banking.interbank.domain.retry;

/** Position of an order event in its retry timeline. */
public record RetrySchedule(RetryPolicy policy, int current) {

    public static RetrySchedule of(RetryPolicy policy, Integer current) {
        return new RetrySchedule(policy, current == null ? policy.initial() : current);
    }

    public RetrySchedule escalate() {
        return new RetrySchedule(policy, current + policy.step());
    }

    public boolean exhausted() {
        return current >= policy.limit();
    }
}
