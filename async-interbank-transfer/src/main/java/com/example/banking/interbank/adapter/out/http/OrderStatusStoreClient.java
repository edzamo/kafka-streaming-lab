package com.example.banking.interbank.adapter.out.http;

import com.example.banking.interbank.adapter.out.http.generated.spInterbankTransferOrder.api.InterbankTransferOrderApi;
import com.example.banking.interbank.adapter.out.http.generated.spInterbankTransferOrder.model.PostInterbankTransferUpdate;
import com.example.banking.interbank.adapter.out.http.generated.spInterbankTransferOrder.model.Status;
import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.out.OrderStatusStore;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

class OrderStatusStoreClient implements OrderStatusStore {

    private final InterbankTransferOrderApi api;
    private final DownstreamCalls calls;

    OrderStatusStoreClient(InterbankTransferOrderApi api, DownstreamCalls calls) {
        this.api = api;
        this.calls = calls;
    }

    @Override
    public Mono<Void> updateStatus(long orderId, String status, OffsetDateTime executionDate, RequestContext ctx) {
        PostInterbankTransferUpdate body = new PostInterbankTransferUpdate()
                .trayOrderId(String.valueOf(orderId))
                .status(new Status().code(status))
                .executionDate(executionDate);
        RequestHeaders h = calls.headers(ctx);
        return calls.guard("updateStatus", () -> api.patchInterbankTransferOrder(h.appName(), h.callerName(),
                h.requestDate(), h.requestId(), h.session(), h.device(), h.deviceIp(), h.language(),
                h.customerAffiliationId(), h.customerUserId(), body, h.processCode(), null)).then();
    }
}
