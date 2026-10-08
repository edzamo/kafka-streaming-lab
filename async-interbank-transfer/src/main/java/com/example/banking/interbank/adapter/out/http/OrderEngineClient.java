package com.example.banking.interbank.adapter.out.http;

import com.example.banking.interbank.adapter.out.http.generated.bsOrder.api.OrderApi;
import com.example.banking.interbank.adapter.out.http.generated.bsOrder.model.Action;
import com.example.banking.interbank.adapter.out.http.generated.bsOrder.model.CustomerUser;
import com.example.banking.interbank.adapter.out.http.generated.bsOrder.model.PatchOrderRequest;
import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.out.OrderEngine;
import reactor.core.publisher.Mono;

class OrderEngineClient implements OrderEngine {

    private final OrderApi api;
    private final DownstreamCalls calls;
    private final String systemUserId;

    OrderEngineClient(OrderApi api, DownstreamCalls calls, String systemUserId) {
        this.api = api;
        this.calls = calls;
        this.systemUserId = systemUserId;
    }

    @Override
    public Mono<Void> markSucceeded(long orderId, RequestContext ctx) {
        return patch("markSucceeded", "TO_SUCCESS", orderId, ctx);
    }

    @Override
    public Mono<Void> markFailed(long orderId, RequestContext ctx) {
        return patch("markFailed", "TO_FAIL", orderId, ctx);
    }

    private Mono<Void> patch(String step, String actionCode, long orderId, RequestContext ctx) {
        PatchOrderRequest body = new PatchOrderRequest()
                .customerUser(new CustomerUser().customerUserId(systemUserId))
                .action(new Action().code(actionCode).description(""));
        // The bs-order contract only declares the channel header; the rest of the traceability headers
        // are still forwarded, as the previous implementation did.
        return OutboundHeaders.attach(
                calls.guard(step, () -> api.patchOrder(orderId, ctx.channel(), body)),
                calls.headers(ctx).asMap());
    }
}
