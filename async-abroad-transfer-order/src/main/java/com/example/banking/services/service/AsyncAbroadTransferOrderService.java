package com.example.banking.services.service;

import com.example.banking.services.client.bsOrderHistory.OrderHistoryApi;
import com.example.banking.services.client.models.bsAbroadTransferOrder.*;
import com.example.banking.services.client.models.bsOrderHistory.GetOrderHistoryByFilterRetrieveResponse;
import com.example.banking.services.client.models.order.PatchOrderRequest;
import com.example.banking.services.client.models.spAbroadTransferOrder.AbroadTransferOrderResponse;
import com.example.banking.services.client.models.spAbroadTransferOrder.GetAbroadTransferOrderResponse;
import com.example.banking.services.client.models.spAbroadTransferOrder.*;
import com.example.banking.services.client.models.spAccount.AccountByIdResponse;
import com.example.banking.services.client.models.spApprovalEngineSheduledTask.PostApprovalScheduledTaskRequest;
import com.example.banking.services.client.models.spApprovalEngineSheduledTask.PostScheduledTaskResponse;
import com.example.banking.services.client.order.OrderApi;
import com.example.banking.services.client.spAbroadTransferOrder.AbroadTransferOrderApi;
import com.example.banking.services.client.spAbroadTransferOrder.AbroadTransferOrderItemApi;
import com.example.banking.services.client.spAccount.AccountApi;
import com.example.banking.services.client.spApprovalEngineSheduledTask.ApprovalEngineScheduledTaskApi;
import com.example.banking.services.config.PropertiesConfiguration;
import com.example.banking.services.dto.EventTransferOrder;
import com.example.banking.services.enums.FormatDateEnum;
import com.example.banking.services.helper.HeaderSupport;
import com.example.banking.services.helper.error.SpAbroadTransferError;
import com.example.banking.services.mapper.AbroadTransferOrderAuthorizationMapper;
import com.example.banking.services.mapper.AbroadTransferOrderMapper;
import com.example.banking.services.service.stream.Bridge;
import com.example.banking.services.utils.GenericUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncAbroadTransferOrderService {

    private final AbroadTransferOrderApi spAbroadTransferOrderApi;
    private final AbroadTransferOrderItemApi spAbroadTransferOrderItemApi;
    private final com.example.banking.services.client.bsAbroadTransferOrder.AbroadTransferOrderApi bsAbroadTransferOrderApi;
    private final com.example.banking.services.client.bsAbroadTransferOrder.AbroadTransferAuthorizationApi abroadTransferAuthorizationApi;
    private final PropertiesConfiguration propertiesConfiguration;
    private final ApprovalEngineScheduledTaskApi approvalEngineScheduledTaskApi;
    private final OrderApi orderApi;
    private final AccountApi accountApi;
    private final OrderHistoryApi orderHistoryApi;
    private final Bridge bridge;
    private final String clienteExcentoISD="clienteExcentoISD";

    public Mono<Void> processTransaction(EventTransferOrder eventTransferOrder, boolean isTask) {

        //Headers
        String channel = propertiesConfiguration.getChannel();
        String deviceIP = propertiesConfiguration.getDeviceIP();
        String appName = propertiesConfiguration.getAppName();

        Map<String, String> headersGet = HeaderSupport.getHeaders(appName, channel, deviceIP, UUID.randomUUID().toString(),eventTransferOrder.getCustomerUserId());
        
        return bsGetAbroadTransferOrderByFilter(headersGet,eventTransferOrder.getOrderId(),"currencyExchange,orderHistory",eventTransferOrder)
                .flatMap(getBsAbroadTransferOrder->{

                    //Header para consumo de apis
                    Map<String, String> headers = HeaderSupport.getHeaders(appName, channel, deviceIP, getBsAbroadTransferOrder.getAbroadTransferOrder().getCustomerAffiliationId(),eventTransferOrder.getCustomerUserId());

                    //Convierte la fecha de execucion y elimina las horas en caso de tenerlas
                    Date executionDate=GenericUtil.cleanHours(GenericUtil.convertStringToDate(getBsAbroadTransferOrder.getAbroadTransferOrder().getExecutionDate(), FormatDateEnum.yyyy_MM_dd_T_HH_mm));
                    Date currentDate=GenericUtil.cleanHours(new Date());

                    return patchAbroadTransferOrder(eventTransferOrder,headers,isTask,getBsAbroadTransferOrder)
                            .flatMap(x->postScheduledTask(eventTransferOrder,currentDate,executionDate,headers,getBsAbroadTransferOrder,isTask))
                            .flatMap(x->patchOrder(eventTransferOrder,getBsAbroadTransferOrder,currentDate,executionDate,headers,isTask));
                })
                .then();
    }

    private Mono<PostAbroadTransferOrderItemResponse> patchAbroadTransferOrder(EventTransferOrder eventTransferOrder, Map<String, String> headers, boolean isTask, GetAbroadTransferOrderByFilterResponse getBsAbroadTransferOrder) {

        if(!isTask || propertiesConfiguration.getStateAvailable().equalsIgnoreCase(eventTransferOrder.getNextStatus())) {

            return spGetAbroadTransferOrderByFilter(headers,eventTransferOrder.getOrderId(),eventTransferOrder)
                    .flatMap(spGetAbroadTransferOrderByFilter -> {

                        return spPatchAbroadTransferOrder(headers, spGetAbroadTransferOrderByFilter.getAbroadTransferOrderId(),AbroadTransferOrderMapper.INSTANCE.eventTransferOrderToSpPatchAbroadTransferOrder(String.valueOf(eventTransferOrder.getOrderId()),eventTransferOrder.getNextStatus(),null),eventTransferOrder)
                                .flatMap(spPatchAbroadTransferOrder->spPatchAbroadTransferOrderItem(headers,getBsAbroadTransferOrder.getAbroadTransferOrder()
                                                    .getAbroadTransferOrderItems().get(0).getAbroadTransferOrderItemId(),
                                            AbroadTransferOrderMapper.INSTANCE.eventTransferOrderToSpPatchAbroadTransferOrderItem(propertiesConfiguration.getSpot()),
                                            eventTransferOrder)
                                );
                    });
        }

        return Mono.just(new PostAbroadTransferOrderItemResponse());
    }

    private Mono<PostScheduledTaskResponse>  postScheduledTask(EventTransferOrder eventTransferOrder,Date currentDate, Date executionDate,Map<String, String> headers,GetAbroadTransferOrderByFilterResponse get,boolean isTask) {

        //No debe ser una tarea y sea estado aprobacion pero si ejecucion no es de hoy.
        if(!isTask &&
                propertiesConfiguration.getStateFinalApproval().equalsIgnoreCase(eventTransferOrder.getNextStatus())
              && executionDate.compareTo(currentDate)!=0) {

            PostApprovalScheduledTaskRequest postApprovalScheduledTaskRequest=AbroadTransferOrderMapper
                    .INSTANCE.eventTransferOrderToPostApprovalScheduledTaskRequest(GenericUtil.dateToOffsetDateTime(executionDate,FormatDateEnum.yyyy_MM_dd_T_HH_mm_ss_Z)
                            .plusHours(Long.valueOf(propertiesConfiguration.getExecutionTime()))
                            ,eventTransferOrder);
            return spPostApprovalScheduledTask(headers,postApprovalScheduledTaskRequest,eventTransferOrder);
        }

       return Mono.just(new PostScheduledTaskResponse());

    }

    private Mono<Void> patchOrder(EventTransferOrder eventTransferOrder, GetAbroadTransferOrderByFilterResponse getBsAbroadTransferOrder, Date currentDate, Date executionDate, Map<String, String> headers, boolean isTask) {

        return bsGetOrderHistoryByFilter(headers, String.valueOf(eventTransferOrder.getOrderId()),eventTransferOrder)
                .flatMap(orderHistory -> {

                    //Para que actualize al motor de bb, debe ser una tarea, estado disponible y que en el motor tenga un estado diferente.
                    if(isTask
                            && eventTransferOrder.getNextStatus()!=null
                            && eventTransferOrder.getNextStatus().equalsIgnoreCase(propertiesConfiguration.getStateAvailable())
                            && !orderHistory.getStatus().getCode().equalsIgnoreCase(eventTransferOrder.getNextStatus())){

                        PatchOrderRequest patchOrderRequest=AbroadTransferOrderMapper.INSTANCE.eventTransferOrderToPatchOrderRequest(propertiesConfiguration.getGenericUserPatch(),propertiesConfiguration.getActionAvailable());
                        return bsPatchOrder(headers, eventTransferOrder.getOrderId(),patchOrderRequest,eventTransferOrder)
                                .doOnSuccess(x->patchAbroadTransferAuthorization(eventTransferOrder,currentDate,executionDate,headers));
                    }

                    return patchAbroadTransferAuthorization(eventTransferOrder,currentDate,executionDate,headers);

                });
    }

    private Mono<Void> patchAbroadTransferAuthorization(EventTransferOrder eventTransferOrder, Date currentDate,Date executionDate,Map<String, String> headers) {

        //Solo se ejecuta si es una orden aprobada y tenga fecha de hoy.
        if(eventTransferOrder.getNextStatus()!=null
                && propertiesConfiguration.getStateFinalApproval().equalsIgnoreCase(eventTransferOrder.getNextStatus())
                        && executionDate.compareTo(currentDate)==0) {

            return bsGetAbroadTransferOrderByFilter(headers,eventTransferOrder.getOrderId(),"currencyExchange,orderHistory",eventTransferOrder)
                    .flatMap(bsGetAbroadTransferOrderByFilter->{

                        return spGetAccountById(headers, bsGetAbroadTransferOrderByFilter.getAbroadTransferOrder()
                                        .getSourceProduct().getProductNumber(),eventTransferOrder)
                                .flatMap(accountOrigen -> {

                                    Optional<OrderHistory> orderHistoryApprove = bsGetAbroadTransferOrderByFilter.getOrderHistory().getOrderHistoryChanges()
                                            .stream().parallel()
                                            .filter(x -> propertiesConfiguration.getActionApprove().equalsIgnoreCase(x.getAction()))
                                            .findFirst();
                                    AbroadTransferOrderItemGet item = bsGetAbroadTransferOrderByFilter.getAbroadTransferOrder().getAbroadTransferOrderItems().get(0);
                                    String userKey=GenericUtil.userKey(bsGetAbroadTransferOrderByFilter.getAbroadTransferOrder().getTrayOrderId());
                                    String operation=new StringBuilder()
                                            .append(propertiesConfiguration.getCodeAgency())
                                            .append("BBE")
                                            .append(String.format("%08d", eventTransferOrder.getOrderId()))
                                            .toString();

                                    //Flujo inicial por default
                                    String actionExecute=propertiesConfiguration.getActionExecuteInitiate();
                                    String isExempt="N";
                                    List<SupplementaryDataExecuteAdditionalFields> listSupplementaryDataExecuteAdditionalFields = new ArrayList<SupplementaryDataExecuteAdditionalFields>();
                                    listSupplementaryDataExecuteAdditionalFields.add(new SupplementaryDataExecuteAdditionalFields()
                                            .key(clienteExcentoISD)
                                            .value("false"));

                                    if (propertiesConfiguration.getStateEnabled().equalsIgnoreCase(item.getExemption().getIsEnabled())
                                        && propertiesConfiguration.getStateSuccess().equalsIgnoreCase(item.getExemption().getFileExemption().getStatus().getCode())) {

                                            //Flujo de aprobacion
                                            actionExecute=propertiesConfiguration.getActionExecuteApprove();
                                            userKey=bsGetAbroadTransferOrderByFilter.getAbroadTransferOrder().getFxReferenceId();
                                            isExempt="S";
                                            listSupplementaryDataExecuteAdditionalFields = new ArrayList<SupplementaryDataExecuteAdditionalFields>();

                                            //Se envia informacion porque tiene archivo isd
                                            listSupplementaryDataExecuteAdditionalFields.add(new SupplementaryDataExecuteAdditionalFields()
                                                    .key(clienteExcentoISD)
                                                    .value("true"));

                                            listSupplementaryDataExecuteAdditionalFields.add(new SupplementaryDataExecuteAdditionalFields()
                                                    .key("codigoDocumentoExento")
                                                    .value(item.getExemption().getFileExemption().getFileReferenceId()));
                                    }

                                    PostAbroadTransferExcuteRequest postAbroadTransferExcuteRequest = AbroadTransferOrderMapper.INSTANCE.bsAbroadTransferOrderToAbroadTransferExcute(userKey,bsGetAbroadTransferOrderByFilter
                                            , item, accountOrigen,orderHistoryApprove.isPresent()?orderHistoryApprove.get():null,propertiesConfiguration.getGenericUserPost(),
                                            operation,propertiesConfiguration.getCodeAgency(),propertiesConfiguration.getAppNameReason(),propertiesConfiguration.getApplicationCode(),
                                            actionExecute,listSupplementaryDataExecuteAdditionalFields,isExempt);
                                    //Se guarda valores por defento
                                    postAbroadTransferExcuteRequest=defaulValue(postAbroadTransferExcuteRequest);

                                    String finalUserKey = userKey;
                                    return bsPostAbroadTransferExcute(headers, postAbroadTransferExcuteRequest,eventTransferOrder)
                                            .doOnSuccess(x->spPatchAbroadTransferOrder(headers,
                                                    bsGetAbroadTransferOrderByFilter.getAbroadTransferOrder().getAbroadTransferOrderId(),
                                                    AbroadTransferOrderMapper.INSTANCE.eventTransferOrderToSpPatchAbroadTransferOrder(String.valueOf(eventTransferOrder.getOrderId()),
                                                            null, finalUserKey), eventTransferOrder)
                                                            .subscribe())
                                            .doOnError(x -> bsPatchOrder(headers, eventTransferOrder.getOrderId(),
                                                        AbroadTransferOrderMapper.INSTANCE.eventTransferOrderToPatchOrderRequest(propertiesConfiguration.getGenericUserPost(), propertiesConfiguration.getActionPending()),
                                                        eventTransferOrder)
                                                    .subscribe())
                                            .doOnError(x->spPatchAbroadTransferOrder(headers,
                                                    bsGetAbroadTransferOrderByFilter.getAbroadTransferOrder().getAbroadTransferOrderId(),
                                                    AbroadTransferOrderMapper.INSTANCE.eventTransferOrderToSpPatchAbroadTransferOrder(String.valueOf(eventTransferOrder.getOrderId()),
                                                            propertiesConfiguration.getStatePending(),null), eventTransferOrder)
                                                    .subscribe())
                                            .then();
                                });
                    });
        }
        return Mono.empty();
    }

    private PostAbroadTransferExcuteRequest defaulValue(PostAbroadTransferExcuteRequest postAbroadTransferExcuteRequest){

        if(!ObjectUtils.isEmpty(postAbroadTransferExcuteRequest.getSupplementaryData())
                && !ObjectUtils.isEmpty(postAbroadTransferExcuteRequest.getSupplementaryData().getTransaction())
                && !ObjectUtils.isEmpty(postAbroadTransferExcuteRequest.getSupplementaryData().getTransaction().getComissionFee())
                && ObjectUtils.isEmpty(postAbroadTransferExcuteRequest.getSupplementaryData().getTransaction().getComissionFee().getAmount())){
            postAbroadTransferExcuteRequest.getSupplementaryData().getTransaction().getComissionFee().setAmount("0.00");
        }

        postAbroadTransferExcuteRequest.getSupplementaryData().setIntermediaryAccount(AbroadTransferOrderAuthorizationMapper.mapperSupplementaryDataExecuteIntermediaryAccount(postAbroadTransferExcuteRequest.getSupplementaryData().getIntermediaryAccount()));
        return postAbroadTransferExcuteRequest;
    }

    private Mono<GetAbroadTransferOrderByFilterResponse>  bsGetAbroadTransferOrderByFilter(Map<String,String> headers,long trayOrderId,String moreData,EventTransferOrder eventTransferOrder){

        log.info("The order information will be consulted.");
        return bsAbroadTransferOrderApi.getAbroadTransferOrderByFilter(headers, String.valueOf(trayOrderId),moreData)
                .doOnSuccess(x->log.info("The order information is consulted successful"))
                .onErrorMap(error -> {
                    log.error("Error when consulting the order information.");
                    throw SpAbroadTransferError.FE04_BS_ABROAD_TRANSFER_ORDER;
                });

    }

    private Mono<Void>  bsPatchOrder(Map<String,String> headers,long trayOrderId,PatchOrderRequest patchOrderRequest,EventTransferOrder eventTransferOrder){

        log.info("The order will be updated");
        return orderApi.patchOrder(headers, trayOrderId, patchOrderRequest)
                .doOnSuccess(x -> log.info("The order was updated successfully."))
                .onErrorMap(error -> {
                    log.error("Service error bs Order");
                    throw SpAbroadTransferError.FE05_BS_ORDER;
                });
    }

    private Mono<GetAbroadTransferOrderResponse>  spGetAbroadTransferOrderByFilter(Map<String,String> headers, long trayOrderId,EventTransferOrder eventTransferOrder){

        log.info("The order information will be consulted.");
        return spAbroadTransferOrderApi.getAbroadTransferOrderByFilter(headers, String.valueOf(trayOrderId))
                .doOnSuccess(x -> log.info("The order information is consulted successful."))
                .onErrorMap(error -> {
                    log.error("Service error sp abroad trasnfer order");
                    throw SpAbroadTransferError.FE02_SP_ABROAD_TRANSFER_ORDER;
                });
    }

    private Mono<PostAbroadTransferOrderItemResponse>  spPatchAbroadTransferOrderItem(Map<String,String> headers, String abroadTransferOrderItemId,PatchAbroadTransferOrderItem patchAbroadTransferOrderItem,EventTransferOrder eventTransferOrder){

        log.info("The item will be been updated.");
        return spAbroadTransferOrderItemApi.patchAbroadTransferOrderItem(headers,abroadTransferOrderItemId,patchAbroadTransferOrderItem)
                .doOnSuccess(x -> log.info("The item has been updated successfully."))
                .onErrorMap(error -> {
                    log.error("Service error sp abroad trasnfer order");
                    throw SpAbroadTransferError.FE02_SP_ABROAD_TRANSFER_ORDER;
                });

    }

    private Mono<AbroadTransferOrderResponse>  spPatchAbroadTransferOrder(Map<String,String> headers, String abroadTransferOrderId,PatchAbroadTransferOrder patchAbroadTransferOrder,EventTransferOrder eventTransferOrder) {

        log.info("The order status will be been updated.");
        return spAbroadTransferOrderApi.patchAbroadTransferOrder(headers, abroadTransferOrderId,patchAbroadTransferOrder)
                .doOnSuccess(x -> log.info("The order status has been updated successfully."))
                .onErrorMap(error -> {
                    log.error("Service error sp abroad trasnfer order");
                    throw SpAbroadTransferError.FE02_SP_ABROAD_TRANSFER_ORDER;
                });
    }

    private Mono<AccountByIdResponse>  spGetAccountById(Map<String,String> headers, String productNumber, EventTransferOrder eventTransferOrder) {
        log.info("The account information will be consulted.");
        return accountApi.getAccountById(headers,productNumber)
                .doOnSuccess(x -> log.info("Consult information of the successful origin account"))
                .onErrorMap(error -> {
                    log.error("Service error sp account");
                    throw SpAbroadTransferError.FE05_SP_ACCOUNT;
                });
    }

    private Mono<Void>  bsPostAbroadTransferExcute(Map<String,String> headers, PostAbroadTransferExcuteRequest postAbroadTransferExcuteRequest, EventTransferOrder eventTransferOrder) {
        log.info("The order will be executed.");
        return abroadTransferAuthorizationApi.postAbroadTransferExcute(headers, postAbroadTransferExcuteRequest)
                .doOnSuccess(x -> log.info("The order has been executed successfully"))
                .onErrorMap(error -> {
                    log.error("Service error bs abroad trasnfer order");
                    throw  SpAbroadTransferError.FE04_BS_ABROAD_TRANSFER_ORDER;
                });
    }

    private Mono<PostScheduledTaskResponse>  spPostApprovalScheduledTask(Map<String,String> headers, PostApprovalScheduledTaskRequest postApprovalScheduledTaskRequest, EventTransferOrder eventTransferOrder) {
        log.info("The task will be executed.");
        return approvalEngineScheduledTaskApi.postApprovalScheduledTask(headers,postApprovalScheduledTaskRequest)
                .doOnSuccess(x->log.info("The task has been created successfully"))
                .onErrorMap(error -> {
                    log.error("Service error sp Scheduled Task");
                    throw  SpAbroadTransferError.FE05_SP_TASK;
                });
    }

    private Mono<GetOrderHistoryByFilterRetrieveResponse>  bsGetOrderHistoryByFilter(Map<String,String> headers, String productNumber, EventTransferOrder eventTransferOrder) {
        log.info("The order history information will be consulted.");
        return orderHistoryApi.getOrderHistoryByFilter(headers, String.valueOf(eventTransferOrder.getOrderId()))
                .doOnSuccess(x -> log.info("Consult information of the successful"))
                .onErrorMap(error -> {
                    log.error("Service error bs order history");
                    throw SpAbroadTransferError.FE08_BS_ORDER_HISTORY;
                });
    }
}
