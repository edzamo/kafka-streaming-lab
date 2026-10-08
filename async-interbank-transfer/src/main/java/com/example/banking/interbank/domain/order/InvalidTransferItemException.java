package com.example.banking.interbank.domain.order;

public class InvalidTransferItemException extends RuntimeException {

    public InvalidTransferItemException(long orderId, int itemIndex, String reason) {
        super("Order " + orderId + " item " + itemIndex + " is invalid: " + reason);
    }
}
