package com.example.banking.interbank.adapter.out.http;

import com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.api.InterbankTransferOrderApi;
import com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.model.GetInterbankTransferOrder;
import com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.model.OrderItem;
import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.out.TransferOrderQuery;
import com.example.banking.interbank.domain.order.InterbankTransferOrder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

class TransferOrderQueryClient implements TransferOrderQuery {

    private static final String WITH_ITEMS = "interbankTransferOrderItems";

    private final InterbankTransferOrderApi api;
    private final DownstreamCalls calls;

    TransferOrderQueryClient(InterbankTransferOrderApi api, DownstreamCalls calls) {
        this.api = api;
        this.calls = calls;
    }

    @Override
    public Mono<InterbankTransferOrder> findWithItems(long orderId, RequestContext ctx) {
        RequestHeaders h = calls.headers(ctx);
        return calls.guard("findWithItems", () -> api.getInterbankTransferOrderByFilter(h.appName(), h.callerName(),
                        h.requestDate(), h.requestId(), h.session(), h.device(), h.deviceIp(), h.language(),
                        h.customerAffiliationId(), h.customerUserId(), Math.toIntExact(orderId), h.processCode(),
                        WITH_ITEMS))
                .map(response -> toDomain(orderId, response));
    }

    private static InterbankTransferOrder toDomain(long orderId, GetInterbankTransferOrder response) {
        List<InterbankTransferOrder.Item> items = Optional.ofNullable(response.getInterbankTransferOrderItems())
                .orElse(List.of()).stream().map(TransferOrderQueryClient::toItem).toList();
        return new InterbankTransferOrder(orderId, response.getCustomerAffiliationId(),
                response.getSourceProduct() == null ? null : response.getSourceProduct().getProductNumber(),
                response.getCustomer() == null ? null : response.getCustomer().getFullName(), items);
    }

    private static InterbankTransferOrder.Item toItem(OrderItem item) {
        return new InterbankTransferOrder.Item(
                item.getDestinationProduct() == null ? null : item.getDestinationProduct().getProductNumber(),
                item.getCurrency() == null ? null : item.getCurrency().getCode(),
                item.getAmount());
    }
}
