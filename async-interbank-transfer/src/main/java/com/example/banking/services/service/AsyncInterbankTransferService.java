package com.example.banking.services.service;

import com.google.gson.Gson;
import com.example.banking.services.client.bsInterbankTransferOrder.InterbankTransferApi;
import com.example.banking.services.client.bsOrder.OrderApi;
import com.example.banking.services.client.models.bsInterbankTransferOrder.*;
import com.example.banking.services.client.models.bsInterbankTransferOrder.Currency;
import com.example.banking.services.client.models.bsOrder.Action;
import com.example.banking.services.client.models.bsOrder.CustomerUser;
import com.example.banking.services.client.models.bsOrder.PatchOrderRequest;
import com.example.banking.services.client.models.spInterbankTransferOrder.InterbankTransferOrderResponse;
import com.example.banking.services.client.models.spInterbankTransferOrder.PostInterbankTransferUpdate;
import com.example.banking.services.config.PropertiesConfiguration;
import com.example.banking.services.dto.EventTransferOrder;
import com.example.banking.services.dto.Headers;
import com.example.banking.services.dto.Message;
import com.example.banking.services.helper.Constants;
import com.example.banking.services.helper.HeaderConstant;
import com.example.banking.services.mapper.CostumerStatusChangeMapper;
import com.example.banking.services.service.stream.Bridge;
import com.example.banking.services.utils.HeaderUtils;
import com.example.banking.services.utils.JsonSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static com.example.banking.services.utils.HeaderUtils.HEADER_CUSTOMER_AFFILIATION_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncInterbankTransferService {

  private final Bridge bridge;

  private final CostumerStatusChangeMapper costumerStatusChangeMapper;

  private final com.example.banking.services.client.spInterbankTransferOrder.InterbankTransferOrderApi spInterbankTransferOrderApi;

  private final com.example.banking.services.client.bsInterbankTransferOrder.InterbankTransferOrderApi bsInterbankTransferOrderApi;
  private final InterbankTransferApi interbankTransferApi;
  private final PropertiesConfiguration propertiesConfiguration;
  private final OrderApi orderApi;

  private final JsonSerializer jsonSerializer;
  private final HeaderUtils headerUtils;

  private final ReactiveStringRedisTemplate redisOperations;

  public Mono<Void> processTransaction(EventTransferOrder eventTransferOrder, Headers headers) {
    Map<String, String> headersMap = new HashMap<>(headerUtils.buildHeadersMap(headers));

    log.info("Processing message for order id: " + eventTransferOrder.getOrderId());
    return updateStatusInProductDatabase(eventTransferOrder, headersMap)
            .flatMap(patchOrderResponse -> {
                      if (eventTransferOrder.getNextStatus().equals(Constants.FINAL_APPROVAL)) {
                        log.info("Obtaining order " + eventTransferOrder.getOrderId() + " details " +
                                "from bs-interbank-transfer-order api, method getInterbankTransferOrderByFilter");
                        log.debug("Headers map used to get order from bs-interbank-transfer-order api " + headersMap);
                        return bsInterbankTransferOrderApi.getInterbankTransferOrderByFilter(
                                        headersMap,
                                        Math.toIntExact(eventTransferOrder.getOrderId()), Constants.MORE_DATA)
                                .doOnError(throwable -> {
                                  log.error("Step 2 -bsInterbankTransferOrderApi.getInterbankTransferOrderByFilter: ", throwable);
                                  errorManager(eventTransferOrder);
                                })
                                .doOnNext(orderInformation -> {
                                  log.info("Order details obtained");
                                  log.debug("Order: " + orderInformation);
                                });
                      } else {
                        return Mono.empty();
                      }
                    }
            )
            .flatMap(order -> executeDirectInterbankTransfers(headersMap, order, eventTransferOrder))
            .flatMap(ok -> {
              PatchOrderRequest patchOrderRequest = new PatchOrderRequest()
                      .customerUser(new CustomerUser()
                              .customerUserId(propertiesConfiguration.getUserSystemDirectTransfer()))
                      .action(new Action().code(Constants.TO_SUCCESS).description(Constants.EMPTY_STRING));

              headersMap.put(HeaderConstant.CHANNEL, eventTransferOrder.getChannel());

              Long orderId = eventTransferOrder.getOrderId();
                PostInterbankTransferUpdate postInterbankTransferUpdate = costumerStatusChangeMapper
                      .orderIdAndStatusAndExecutionDateToPatchInterbankTransferOrder(
                              eventTransferOrder.getOrderId(), propertiesConfiguration.getStatusLastStep(), OffsetDateTime.now());

              return orderApi.patchOrder(headersMap, orderId, patchOrderRequest)
                      .doOnError(throwable -> spInterbankTransferOrderApi.patchInterbankTransferOrder(postInterbankTransferUpdate)
                              .map(interbankTransferOrderResponse -> {
                                sendMessage(eventTransferOrder.getOrderId());
                                return interbankTransferOrderResponse;
                              }).subscribe());
            });
  }

  private Mono<InterbankTransferOrderResponse> updateStatusInProductDatabase(
          EventTransferOrder eventTransferOrder, Map<String, String> headersMap) {

    PostInterbankTransferUpdate patchOrderRequest = costumerStatusChangeMapper
            .eventTransferOrderToPatchInterbankTransferOrder(eventTransferOrder);

    if (Objects.equals(eventTransferOrder.getNextStatus(), Constants.PENDING)) {
      return Mono.empty();
    } else {
      log.info("Updating status in product database using sp-interbank-transfer order API. method patchInterbankTransferOrder");
      return spInterbankTransferOrderApi.patchInterbankTransferOrder(
                      headersMap, patchOrderRequest)
              .doOnError(throwable -> {
                log.error("Step 1 -spInterbankTransferOrderApi.patchInterbankTransferOrder: ", throwable);
                errorManager(eventTransferOrder);
              })
              .doOnNext(response -> log.info("Order status updated."))
              .map(interbankTransferOrderResponse -> {
                sendMessage(eventTransferOrder.getOrderId());
                return interbankTransferOrderResponse;
              });
    }
  }

  private Mono<Boolean> executeDirectInterbankTransfers(
          Map<String, String> headers, GetInterbankTransferOrder order,
          EventTransferOrder eventTransferOrder) {

    Map<String, String> directOrderHeaders = new HashMap<>(headers);

    directOrderHeaders.put(HEADER_CUSTOMER_AFFILIATION_ID, order.getCustomerAffiliationId());
    setHeadersDirectTransfer(directOrderHeaders);


    return buildDirectTransferOrderRequest(order, eventTransferOrder)
            .doOnNext(directOrder -> {
              log.info("Executing order using bs-interbank-transfer-order-api, method postDirectTransferOrder");
              log.debug("Direct order " + directOrder);
              log.debug("Headers for direct order " + directOrderHeaders);
            })
            .flatMap(directTransferOrder ->
                    interbankTransferApi.postInterbankTransfer(directOrderHeaders, directTransferOrder))
            .doOnError(throwable -> {
              log.error("Step 3 - directTransferOrderApi.postDirectTransferOrder: ", throwable);
              errorManager(eventTransferOrder);
            })
            .then(Mono.just(Boolean.TRUE))
            .doOnNext(ok -> log.info("Order " + eventTransferOrder.getOrderId() + " executed"));
  }

  private Flux<BsPostInterbankTransferRequest> buildDirectTransferOrderRequest(
          GetInterbankTransferOrder order, EventTransferOrder eventTransferOrder) {

    List<OrderItem> orderItems = order.getInterbankTransferOrderItems();
    if (orderItems == null || orderItems.isEmpty()) {
      return Flux.empty();
    }

    return Flux.fromIterable(orderItems)
            .map(orderItem ->
                    costumerStatusChangeMapper.eventTransferOrderToPostDirectTransferOrderRequest(eventTransferOrder)
                            .destinationProduct(new Product()
                                    .productId(Optional.ofNullable(orderItem.getDestinationProduct()).orElse(new DestinationProduct())
                                            .getProductNumber()))
                            .sourceProduct(new Product()
                                    .productId(Optional.ofNullable(order.getSourceProduct()).orElse(new SourceProductType())
                                            .getProductNumber()))
                            .currency(new Currency()
                                    .code(Optional.ofNullable(orderItem.getCurrency()).orElse(
                                            new com.example.banking.services.client.models.bsInterbankTransferOrder.Currency()).getCode()))
                            .amount(Optional.ofNullable(orderItem.getAmount()).orElse(BigDecimal.ZERO).toString())
                            .beneficiary(new Beneficiary().fullName(
                                            Optional.ofNullable(order.getCustomer()).orElse(new Customer()).getFullName())
                                    .email(""))
                            .description("")
                            .currency(new Currency()
                                    .code(orderItem.getCurrency().getCode()))
            );
  }

  private void setHeadersDirectTransfer(Map<String, String> headers) {
    headers.put(HeaderConstant.X_GUID, UUID.randomUUID().toString());
    headers.put(HeaderConstant.X_AGENCY, propertiesConfiguration.getXAgency());
    headers.put(HeaderConstant.X_GEOLOCATION, propertiesConfiguration.getXGeolocation());

  }

  private void errorManager(EventTransferOrder eventTransferOrder) {
    eventTransferOrder.setSegIntervalError(eventTransferOrder.getSegIntervalError() + propertiesConfiguration.getSegInterval());
    String deal = new Gson().toJson(eventTransferOrder);
    bridge.sendMessage(deal);
  }

  private void sendMessage(Long trayOrderId) {
    Message message = Message.builder()
            .trayOrderId(trayOrderId)
            .build();

    String messageString = jsonSerializer.jsonObjectToString(message);

    redisOperations.convertAndSend(propertiesConfiguration.getClaimName(), messageString)
            .map(x -> {
              log.debug("Send " + x);
              return x;
            })
            .doOnError(throwable -> log.error("redisOperations.convertAndSend with orderId: " + trayOrderId + " " + throwable.getMessage()))
            .subscribe();
  }

    public Mono<Void> processErrorPatchOrder(EventTransferOrder eventTransferOrder, Headers headers) {

        log.info("Start read service error on Queue!..");

        Map<String, String> headersMap = new HashMap<>(headerUtils.buildHeadersMap(headers));
        headersMap.put(HeaderConstant.CHANNEL, eventTransferOrder.getChannel());

        PostInterbankTransferUpdate postInterbankTransferUpdate = costumerStatusChangeMapper
                .orderIdAndStatusAndExecutionDateToPatchInterbankTransferOrder(
                        eventTransferOrder.getOrderId(), propertiesConfiguration.getErrorLastStep(), OffsetDateTime.now());

        log.info("Updating status in product database to error");
        return spInterbankTransferOrderApi.patchInterbankTransferOrder(headersMap, postInterbankTransferUpdate)
                .doOnError(throwable -> log.error("Error during spInterbankTransferOrderApi.postInterbankTransferUpdate: ", throwable))
                .doOnNext(orderUpdate -> {
                    log.info("Order status updated");
                    log.info("Updating status in engine");
                })
                .flatMap(interbankTransferOrderResponse -> {
                    PatchOrderRequest patchOrderRequest = new PatchOrderRequest();
                    patchOrderRequest.setCustomerUser(new CustomerUser().customerUserId(propertiesConfiguration.getUserSystemDirectTransfer()));
                    patchOrderRequest.setAction(new Action().code(Constants.TO_FAIL).description(Constants.EMPTY_STRING));
                    return orderApi.patchOrder(headersMap, eventTransferOrder.getOrderId(), patchOrderRequest)
                            .doOnError(throwable -> log.error("Step 4 - orderApi.patchOrder: " + throwable))
                            .thenReturn("OK")
                            .flatMap(ok -> {
                                log.info("Order status updated in engine");
                                sendMessage(eventTransferOrder.getOrderId());
                                return Mono.empty().then();
                            });
                });
    }

}
