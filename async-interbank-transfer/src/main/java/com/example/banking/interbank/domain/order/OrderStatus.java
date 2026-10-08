package com.example.banking.interbank.domain.order;

/**
 * Order status. The set of statuses is owned by the approval engine and open-ended, so the raw code is kept
 * and only the two statuses that drive behaviour are known here.
 */
public record OrderStatus(String code) {

    private static final String PENDING = "PENDING";
    private static final String FINAL_APPROVAL = "FINAL_APPROVAL";

    public OrderStatus {
        if (code == null || code.isBlank()) {
            throw new InvalidOrderStatusException();
        }
    }

    public static OrderStatus of(String code) {
        return new OrderStatus(code);
    }

    public boolean requiresExecution() {
        return FINAL_APPROVAL.equals(code);
    }

    public boolean requiresProductUpdate() {
        return !PENDING.equals(code);
    }
}
