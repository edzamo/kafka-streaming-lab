package com.example.banking.interbank.adapter.in.messaging;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Wire format of an order event. {@code segIntervalError} is the former name of the retry counter. */
@JsonIgnoreProperties(ignoreUnknown = true)
record TransferOrderEventMessage(Long orderId, String customerUserId, String eventDate, String status,
                                 String nextStatus, String channel, String action,
                                 @JsonAlias("segIntervalError") Integer retrySeconds) {
}
