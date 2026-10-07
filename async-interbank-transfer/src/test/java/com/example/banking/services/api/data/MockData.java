package com.example.banking.services.api.data;

import com.example.banking.services.client.models.bsInterbankTransferOrder.*;
import com.example.banking.services.client.models.bsInterbankTransferOrder.Action;
import com.example.banking.services.client.models.bsInterbankTransferOrder.Beneficiary;
import com.example.banking.services.client.models.bsInterbankTransferOrder.Currency;
import com.example.banking.services.client.models.bsInterbankTransferOrder.Product;
import com.example.banking.services.client.models.bsOrder.*;
import com.example.banking.services.client.models.spInterbankTransferOrder.*;
import com.example.banking.services.client.models.spInterbankTransferOrder.Status;
import com.example.banking.services.dto.EventTransferOrder;
import com.example.banking.services.server.models.Error;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MockData {

  private MockData() {
  }

  public static PostInterbankTransferUpdate postInterbankTransferUpdate()
  {
    PostInterbankTransferUpdate postInterbankTransferUpdate=new PostInterbankTransferUpdate();
    postInterbankTransferUpdate.setStatus(new Status().code(""));
    postInterbankTransferUpdate.setTrayOrderId("1");
    return postInterbankTransferUpdate;
  }
  public static EventTransferOrder eventTransferOrder()
  {
    EventTransferOrder eventTransferOrder=new EventTransferOrder();
    eventTransferOrder.setOrderId(114L);
    eventTransferOrder.setCustomerUserId("g2f01259-a60b-45e8-b5e3-255924834e4");
    eventTransferOrder.setEventDate("2022-03-26T00:00:00");
    eventTransferOrder.setStatus("PENDING_SEND");
    eventTransferOrder.setNextStatus("PENDING_SEND");
    eventTransferOrder.setChannel("business banking");
    eventTransferOrder.setAction("TO_PENDING");
    eventTransferOrder.setSegIntervalError(30);
    return eventTransferOrder;
  }
  public static BsPostInterbankTransferRequest postInterbankTransferOrderRequest()
  {
    BsPostInterbankTransferRequest postInterbankTransferOrderRequest=new BsPostInterbankTransferRequest();
    postInterbankTransferOrderRequest.setOrderId(114);
    postInterbankTransferOrderRequest.setDestinationProduct(new Product().productId(""));
    postInterbankTransferOrderRequest.setSourceProduct(new Product().productId(""));
    postInterbankTransferOrderRequest.setAmount("10");
    postInterbankTransferOrderRequest.setBeneficiary(new Beneficiary().email("").fullName(""));
    postInterbankTransferOrderRequest.setCurrency(new Currency().code(""));
    postInterbankTransferOrderRequest.setDescription("");
    postInterbankTransferOrderRequest.setAction(new Action().code(""));
    return postInterbankTransferOrderRequest;
  }
  public static com.example.banking.services.client.models.bsInterbankTransferOrder.GetInterbankTransferOrder getInterbankTransferOrder()
  {
    com.example.banking.services.client.models.bsInterbankTransferOrder.GetInterbankTransferOrder getInterbankTransferOrder=new com.example.banking.services.client.models.bsInterbankTransferOrder.GetInterbankTransferOrder();
    getInterbankTransferOrder.setInterbankTransferOrderId("");
    getInterbankTransferOrder.setCustomerProductId("");
    getInterbankTransferOrder.setSourceProduct(new SourceProductType().productNumber("").productType(new Type().code("").description("")));
    getInterbankTransferOrder.setCustomerAffiliationId("");
    getInterbankTransferOrder.setStatus(new com.example.banking.services.client.models.bsInterbankTransferOrder.Status().code(""));
    List<com.example.banking.services.client.models.bsInterbankTransferOrder.OrderItem> lists=new ArrayList<>();
    com.example.banking.services.client.models.bsInterbankTransferOrder.OrderItem item=new com.example.banking.services.client.models.bsInterbankTransferOrder.OrderItem();
    item.setInterbankTransferItemId("");
    item.setBeneficiary(new BeneficiaryResponse().email("").fullName("").documentType("").documentNumber(""));
    item.destinationProduct(new com.example.banking.services.client.models.bsInterbankTransferOrder.DestinationProduct().productNumber("").productType(new TypeProduct()).financialEntity(new FinancialEntityTransaction().financialEntityId("").fullName("")));
    item.amount(BigDecimal.valueOf(30));
    item.currency(new com.example.banking.services.client.models.bsInterbankTransferOrder.Currency().code(""));
    item.description("");
    lists.add(item);
    getInterbankTransferOrder.setInterbankTransferOrderItems(lists);
    OrderHistory orderHistory=new OrderHistory();
    orderHistory.setStartOrderDate("");
    orderHistory.setEndOrderDate("");
    orderHistory.setLastChangeDate("");
    orderHistory.setStatus(new com.example.banking.services.client.models.bsInterbankTransferOrder.Status().code(""));
    List<OrderHistoryChange> orderHistoryChangeList=new ArrayList<>();
    OrderHistoryChange orderHistoryChange=new OrderHistoryChange();
    orderHistoryChange.setAction("");
    orderHistoryChange.setChangeDate("");
    orderHistoryChange.setCustomerUser(new User().documentNumber("").documentType("").email("").names("").position("").surnames(""));
    orderHistoryChange.setStatus(new com.example.banking.services.client.models.bsInterbankTransferOrder.Status().code(""));
    orderHistoryChange.setNextStatus(new com.example.banking.services.client.models.bsInterbankTransferOrder.Status().code(""));
    orderHistoryChangeList.add(orderHistoryChange);
    orderHistory.setOrderHistoryChanges(orderHistoryChangeList);
    getInterbankTransferOrder.setOrderHistory(orderHistory);
    return getInterbankTransferOrder;
  }

  public static String getErrorString() {
    return "{" +
            "\"errorCode\":\"001\"," +
            "\"developerMessage\":\"developerMessage\"," +
            "\"userMessage\":\"userMessage\"," +
            "\"moreInfo\":\"moreInfo\"," +
            "\"version\":\"version\"," +
            "\"status\":1" +
            "}";
  }

  public static Error getError() {
    return new Error().errorCode("001")
            .developerMessage("developerMessage")
            .userMessage("userMessage")
            .moreInfo("moreInfo")
            .version("version")
            .status(1);
  }

  public static String getOrderMessageString() {
    return "{" +
            "\"trayOrderId\":1" +
            "}";
  }

}
