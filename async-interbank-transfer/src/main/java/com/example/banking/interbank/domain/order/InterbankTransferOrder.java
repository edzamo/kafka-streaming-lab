package com.example.banking.interbank.domain.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

public record InterbankTransferOrder(long orderId, String customerAffiliationId, String sourceProductId,
                                     String beneficiaryName, List<Item> items) {

    public record Item(String destinationProductId, String currencyCode, BigDecimal amount) {

        @Override
        public String toString() {
            return "Item[redacted]";
        }
    }

    public InterbankTransferOrder {
        items = items == null ? List.of() : List.copyOf(items);
    }

    @Override
    public String toString() {
        return "InterbankTransferOrder[orderId=" + orderId + ", items=" + items.size() + "]";
    }

    public List<TransferInstruction> instructions() {
        return IntStream.range(0, items.size()).mapToObj(this::instructionFor).toList();
    }

    private TransferInstruction instructionFor(int index) {
        Item item = items.get(index);
        if (isBlank(item.currencyCode())) {
            throw new InvalidTransferItemException(orderId, index, "currency is missing");
        }
        if (item.amount() == null || item.amount().signum() <= 0) {
            throw new InvalidTransferItemException(orderId, index, "amount must be positive");
        }
        if (isBlank(item.destinationProductId()) || isBlank(sourceProductId)) {
            throw new InvalidTransferItemException(orderId, index, "source or destination product is missing");
        }
        return new TransferInstruction(orderId, sourceProductId, item.destinationProductId(),
                item.currencyCode(), item.amount(), beneficiaryName);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
