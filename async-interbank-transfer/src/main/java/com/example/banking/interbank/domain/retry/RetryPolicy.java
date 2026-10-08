package com.example.banking.interbank.domain.retry;

/** Retry waiting policy, in seconds. The step must be positive so every schedule eventually exhausts. */
public record RetryPolicy(int initial, int step, int limit) {

    public RetryPolicy {
        if (initial < 0 || step <= 0 || limit <= 0) {
            throw new IllegalArgumentException("Retry policy needs initial >= 0, step > 0 and limit > 0");
        }
    }
}
