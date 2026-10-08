package com.example.banking.interbank.adapter.out.http;

import com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.api.InterbankTransferApi;
import com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.model.Action;
import com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.model.Beneficiary;
import com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.model.BsPostInterbankTransferRequest;
import com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.model.Currency;
import com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.model.Product;
import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.out.TransferExecutor;
import com.example.banking.interbank.domain.order.TransferInstruction;
import reactor.core.publisher.Mono;

import java.util.Map;

class TransferExecutorClient implements TransferExecutor {

    private final InterbankTransferApi api;
    private final DownstreamCalls calls;

    TransferExecutorClient(InterbankTransferApi api, DownstreamCalls calls) {
        this.api = api;
        this.calls = calls;
    }

    @Override
    public Mono<Void> execute(TransferInstruction instruction, RequestContext ctx, int itemIndex) {
        BsPostInterbankTransferRequest body = new BsPostInterbankTransferRequest()
                .sourceProduct(new Product().productId(instruction.sourceProductId()))
                .destinationProduct(new Product().productId(instruction.destinationProductId()))
                .beneficiary(new Beneficiary().fullName(instruction.beneficiaryName()).email(""))
                .amount(instruction.amount().toPlainString())
                .currency(new Currency().code(instruction.currencyCode()))
                .description("")
                .orderId(Math.toIntExact(instruction.orderId()))
                .action(new Action().code(ctx.action()));
        RequestHeaders h = calls.headers(ctx);
        Map<String, String> undeclared = Map.of(
                "Idempotency-Key", h.requestId() + "-" + itemIndex,
                "X-Guid", h.requestId(),
                "X-Agency", calls.defaults().agency(),
                "X-Geolocation", calls.defaults().geolocation());
        return OutboundHeaders.attach(calls.guard("execute", () -> api.postInterbankTransfer(h.appName(),
                h.callerName(), h.requestDate(), h.requestId(), h.session(), h.device(), h.deviceIp(), h.language(),
                h.customerAffiliationId(), h.customerUserId(), body, h.processCode())), undeclared);
    }
}
