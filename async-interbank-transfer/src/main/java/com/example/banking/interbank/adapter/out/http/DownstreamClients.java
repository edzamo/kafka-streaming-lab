package com.example.banking.interbank.adapter.out.http;

import com.example.banking.interbank.application.out.OrderEngine;
import com.example.banking.interbank.application.out.OrderStatusStore;
import com.example.banking.interbank.application.out.TransferExecutor;
import com.example.banking.interbank.application.out.TransferOrderQuery;

/**
 * Builds the outbound HTTP adapters from their base URLs. The generated OpenAPI clients never leave this package.
 */
public final class DownstreamClients {

    private DownstreamClients() {
    }

    public static OrderStatusStore orderStatusStore(String baseUrl, DownstreamCalls calls) {
        var apiClient = new com.example.banking.interbank.adapter.out.http.generated.spInterbankTransferOrder.invoker.ApiClient(DownstreamWebClient.create());
        apiClient.setBasePath(baseUrl);
        return new OrderStatusStoreClient(new com.example.banking.interbank.adapter.out.http.generated.spInterbankTransferOrder.api.InterbankTransferOrderApi(apiClient), calls);
    }

    public static TransferOrderQuery transferOrderQuery(String baseUrl, DownstreamCalls calls) {
        var apiClient = new com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.invoker.ApiClient(DownstreamWebClient.create());
        apiClient.setBasePath(baseUrl);
        return new TransferOrderQueryClient(new com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.api.InterbankTransferOrderApi(apiClient), calls);
    }

    public static TransferExecutor transferExecutor(String baseUrl, DownstreamCalls calls) {
        var apiClient = new com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.invoker.ApiClient(DownstreamWebClient.create());
        apiClient.setBasePath(baseUrl);
        return new TransferExecutorClient(new com.example.banking.interbank.adapter.out.http.generated.bsInterbankTransferOrder.api.InterbankTransferApi(apiClient), calls);
    }

    public static OrderEngine orderEngine(String baseUrl, DownstreamCalls calls, String systemUserId) {
        var apiClient = new com.example.banking.interbank.adapter.out.http.generated.bsOrder.invoker.ApiClient(DownstreamWebClient.create());
        apiClient.setBasePath(baseUrl);
        return new OrderEngineClient(new com.example.banking.interbank.adapter.out.http.generated.bsOrder.api.OrderApi(apiClient), calls, systemUserId);
    }
}
