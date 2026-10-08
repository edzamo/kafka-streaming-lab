package com.example.banking.interbank.domain.order;

public class InvalidOrderStatusException extends RuntimeException {

    public InvalidOrderStatusException() {
        super("The order status is missing");
    }
}
