package com.example.banking.interbank.application.in;

import com.example.banking.interbank.domain.order.OrderStatus;

/** Order event as delivered by the approval flow; {@code retrySeconds} is null until the first retry. */
public record TransferOrderEvent(long orderId, String customerUserId, String eventDate, String status,
                                 OrderStatus nextStatus, String channel, String action, Integer retrySeconds) {
}
