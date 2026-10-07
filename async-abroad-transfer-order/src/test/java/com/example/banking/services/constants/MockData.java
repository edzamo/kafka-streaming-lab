package com.example.banking.services.constants;

import com.example.banking.services.client.models.bsAbroadTransferOrder.GetAbroadTransferOrderByFilterResponse;
import com.example.banking.services.client.models.bsOrderHistory.GetOrderHistoryByFilterRetrieveResponse;
import com.example.banking.services.client.models.spAbroadTransferOrder.GetAbroadTransferOrderResponse;
import com.example.banking.services.client.models.spAccount.AccountByIdResponse;
import com.example.banking.services.dto.EventTransferOrder;
import io.cucumber.messages.internal.com.google.gson.Gson;

public class MockData {

    public static String getApiGatewayErrorString() {
        return "{" +
                "\"component\":\"API-ERROR\"," +
                "\"title\":\"user message\"," +
                "\"detail\":\"developer message\"" +
                "}";
    }

    /*public static EventTransferOrder evenetTransferOrder() {
        String json="{\"id\":28956,\"orderId\":17163,\"customerUserId\":\"24727243-b16b-489d-8658-3450ba45e38d\",\"customerAffiliationId\":\"768d60a6-bb6e-441c-8a61-97ef5592ced9\",\"amount\":15,\"productId\":\"2cdefc48-c795-4677-bdbb-8e0d3506b386\",\"eventDate\":\"2023-05-12T14:16:38.478124387\",\"expirationDate\":\"2021-05-15T00:00:00\",\"currencyCode\":\"USD\",\"status\":\"PENDING\",\"nextStatus\":\"FINAL_APPROVAL\",\"channel\":\"bussines-banking\",\"action\":\"TO_APPROVAL\",\"orderType\":\"PPROV\",\"customerProductId\":\"7B748A2D-A3BF-4998-8410-290E8CD46213\",\"commandExecute\":\"UPDATE\",\"parameter\":{\"PRODUCT_TOPIC_EVENT\":\"tx_eng.pte\"}}";
        Gson gson=new Gson();
        return gson.fromJson(json,EventTransferOrder.class);
    }*/

    public static GetAbroadTransferOrderByFilterResponse getBsAbroadTransferOrderByFilterResponse(){
        String json="{\"abroadTransferOrder\":{\"abroadTransferOrderId\":\"1073D363-E6D9-4701-AA19-00E1B3DB5613\",\"orderType\":\"ABROAD\",\"entityTag\":\"2074D365-E6D9-4801-AA19-00E1B3DB5613\",\"customerProductId\":\"1073D363-E6D9-4701-AA19-00E1B3DB5613\",\"customerAffiliationId\":\"1073D363-E6D9-4701-AA19-00E1B3DB5613\",\"sourceProduct\":{\"productNumber\":\"12345678\",\"productType\":{\"code\":\"003\",\"description\":\"\"},\"currency\":{\"code\":\"USD\",\"description\":\"Dolares Americanos\"},\"alias\":\"alias account\"},\"processingType\":{\"code\":\"IMMEDIATE\"},\"registerDate\":\"2022-11-18T23:50\",\"executionDate\":\"2022-11-18T23:50\",\"expirationDate\":\"2022-11-18T23:50\",\"customerUserId\":\"26483ece-f70a-4195-9248-6415bf6900ae\",\"trayOrderId\":\"14303\",\"amount\":123.8,\"status\":{\"code\":\"PENDING\"},\"channel\":\"BUSINESS BANKING\",\"abroadTransferOrderItems\":[{\"abroadTransferOrderItemId\":\"12345678-eb18-432b-a9ac-0aa5e80ebe9a\",\"entityTag\":\"2074D365-E6D9-4801-AA19-00E1B3DB5613\",\"orderType\":\"ABROAD0\",\"currencyExchangeRequired\":true,\"currencyExchange\":{\"sourceCode\":\"46546\",\"operationNumber\":\"46547\",\"details\":\"S/D\",\"operationType\":\"SPOT\",\"currencyPair\":\"USD.EUR\",\"exchangeRate\":\"1.073530000\"},\"beneficiary\":{\"fullName\":\"Jesus Alberto\",\"documentType\":\"RUC\",\"documentNumber\":\"1231233450\",\"address\":{\"country\":{\"code\":\"EC\",\"description\":\"Ecuador\"},\"city\":\"Barcelona\",\"summary\":\"OE3 y S78\"}},\"intermediaryFinancialEntity\":{\"intermediaryFinancialEntityIdentificationCode\":\"SWIFT or ABA\",\"intermediaryFinancialEntityId\":\"IRVTUS3N\",\"fullName\":\"España\",\"address\":{\"country\":{\"code\":\"EC\",\"description\":\"Ecuador\"},\"city\":\"Barcelona\",\"summary\":\"OE3 y S78\"}},\"destinationProduct\":{\"productNumber\":\"AC1233546\",\"financialEntity\":{\"financialEntityIdentificationCode\":\"SWIFT or ABA\",\"financialEntityId\":\"9928\",\"fullName\":\"Bank of America\",\"address\":{\"country\":{\"code\":\"EC\",\"description\":\"Ecuador\"},\"city\":\"Barcelona\",\"summary\":\"OE3 y S78\"}}},\"amount\":123.8,\"currency\":{\"code\":\"EUR\",\"description\":\"Euro\"},\"beneficiaryTaxId\":\"1223\",\"remittanceCostType\":{\"code\":\"SHA\"},\"economicMotivation\":\"economic Motivation\",\"importCustomsDeclaration\":\"464654\",\"routingCode\":\"routingCode\",\"clabe\":\"clabe\",\"paymentPurpose\":\"Scholarship Payment\",\"description\":\"descripción de la transacción\",\"exemption\":{\"isEnabled\":\"ENABLED\",\"type\":{\"code\":\"Education\"},\"declarationNumberSRI\":\"65959\",\"fileExemption\":{\"name\":\"archivo\",\"path\":\"C:/documents/archivo.txt\",\"status\":{\"code\":\"PENDING\"}},\"amount\":0.3,\"quotaAvailable\":0.5,\"quotaUsed\":0.1},\"isd\":{\"value\":45.25,\"percentage\":4.2},\"fee\":{\"feeId\":\"76945GG5-0187-47UU-81F7-C1E2B91RRECD5\",\"value\":211}}]},\"additionalData\":{\"customer\":{\"fullName\":\"fullname\",\"userName\":\"userName\"}},\"orderHistory\":{\"startOrderDate\":\"2022-06-05\",\"endOrderDate\":\"2022-06-05\",\"lastChangeDate\":\"28/01\",\"status\":{\"code\":\"PENDING\"},\"orderHistoryChanges\":[{\"changeDate\":\"2022-06-05T20:10:05\",\"customerUser\":{\"documentNumber\":\"1721553616\",\"documentType\":\"0001\",\"names\":\"Jhon Carlos\",\"surnames\":\"Steward Colling\",\"email\":\"jhondoe@mail.com\",\"position\":\"Manager\"},\"status\":{\"code\":\"PENDING\"},\"message\":\"This change is due to ...\",\"action\":\"TO_APPROVAL\",\"nextStatus\":{\"code\":\"PENDING\"}}]}}";
        Gson gson=new Gson();
        return gson.fromJson(json,GetAbroadTransferOrderByFilterResponse.class);
    }

    public static GetAbroadTransferOrderResponse getSpAbroadTransferOrderByFilter() {
        String json="{\"abroadTransferOrderId\":\"1073D363-E6D9-4701-AA19-00E1B3DB5613\",\"orderType\":\"ABROAD\",\"entityTag\":\"2074D365-E6D9-4801-AA19-00E1B3DB5613\",\"customerProductId\":\"1073D363-E6D9-4701-AA19-00E1B3DB5613\",\"customerAffiliationId\":\"1073D363-E6D9-4701-AA19-00E1B3DB5613\",\"sourceProduct\":{\"productNumber\":\"12345678\",\"productType\":{\"code\":\"003\",\"description\":\"\"},\"currency\":{\"code\":\"USD\",\"description\":\"Dolares Americanos\"}},\"processingType\":{\"code\":\"IMMEDIATE\"},\"registerDate\":\"2022-11-18T23:50\",\"executionDate\":\"2022-11-18T23:50\",\"expirationDate\":\"2022-11-18T23:50\",\"customerUserId\":\"26483ece-f70a-4195-9248-6415bf6900ae\",\"trayOrderId\":\"14303\",\"amount\":123.8,\"status\":{\"code\":\"PENDING\"},\"channel\":\"BUSINESS BANKING\"}";
        Gson gson=new Gson();
        return gson.fromJson(json,GetAbroadTransferOrderResponse.class);
    }

    public static GetOrderHistoryByFilterRetrieveResponse getOrderHistoryByFilter() {
        String json="{\"orderId\":465416517,\"customerProduct\":{\"customerProductId\":\"asdf-32323\",\"name\":\"REC\"},\"customerAffiliationId\":\"1235454-74v87as85\",\"startOrderDate\":\"28-01-2022\",\"endOrderDate\":\"28-01-2022\",\"expirationDate\":\"28-01-2022\",\"amount\":10,\"currency\":{\"code\":\"USD\"},\"lastChangeDate\":\"28-01-2022\",\"status\":{\"code\":\"PENDING\"},\"orderItem\":{\"identificationValue\":\"17913233\",\"beneficiary\":{\"fullName\":\"Camilo Casas\",\"documentType\":\"0001\",\"documentNumber\":\"9289282\"}},\"orderHistories\":[{\"changeDate\":\"28-01-2022\",\"action\":\"TO_APPROVE\",\"customerUserId\":\"17913233\",\"message\":\"This change is due to ...\",\"status\":{\"code\":\"PENDING\"},\"nextStatus\":{\"code\":\"PENDING\"}}]}";
        Gson gson=new Gson();
        return gson.fromJson(json, GetOrderHistoryByFilterRetrieveResponse.class);
    }

    public static AccountByIdResponse getAccountById() {
        String json="{\"accountId\":\"2100027661\",\"type\":{\"code\":\"\",\"description\":\"CTA.CTE PERSONAL\"},\"customer\":{\"documentNumber\":\"0590058653001\",\"documentType\":\"3\",\"fullname\":\"BUSTAMANTE  MARQUEZ TAURIEL LISETTE\"},\"status\":{\"code\":\"\",\"description\":\"CANCELADO\"},\"openingDate\":\"21022014\",\"endDate\":\"1606670\",\"currency\":{\"code\":\"USD\",\"description\":\" \"},\"balanceInformation\":{\"availableAmount\":\"0.00\",\"countableAmount\":\"0.00\"},\"product\":{\"code\":\"2101\",\"description\":\" \"},\"subProduct\":{\"code\":\"1\",\"description\":\"\"}}";
        Gson gson=new Gson();
        return gson.fromJson(json, AccountByIdResponse.class);
    }

    public static EventTransferOrder evenetTransferOrder() {
        EventTransferOrder eventTransferOrder = new EventTransferOrder();
        eventTransferOrder.setOrderId(114L);
        eventTransferOrder.setCustomerUserId("g2f01259-a60b-45e8-b5e3-255924834e2");
        eventTransferOrder.setEventDate("2022-03-23T00:00:00");
        //eventTransferOrder.setStatus("PENDING_SEND");
        eventTransferOrder.setNextStatus("FINAL_APPROVAL");
        //eventTransferOrder.setChannel(CHANNEL);
        eventTransferOrder.setAction("TO_APPROVAL");
        eventTransferOrder.setSegIntervalError(30);
        return eventTransferOrder;
    }
}
