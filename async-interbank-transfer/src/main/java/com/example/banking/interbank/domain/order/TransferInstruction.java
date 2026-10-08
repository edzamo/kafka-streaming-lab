package com.example.banking.interbank.domain.order;

import java.math.BigDecimal;

public record TransferInstruction(long orderId, String sourceProductId, String destinationProductId,
                                  String currencyCode, BigDecimal amount, String beneficiaryName) {

    @Override
    public String toString() {
        return "TransferInstruction[orderId=" + orderId + "]";
    }
}
