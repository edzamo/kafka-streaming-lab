package com.example.banking.services.impl;

import com.example.banking.services.client.bsOrderHistory.OrderHistoryApi;
import com.example.banking.services.client.models.bsAbroadTransferOrder.GetAbroadTransferOrderByFilterResponse;
import com.example.banking.services.client.models.bsOrderHistory.GetOrderHistoryByFilterRetrieveResponse;
import com.example.banking.services.client.models.spAbroadTransferOrder.AbroadTransferOrderResponse;
import com.example.banking.services.client.models.spAbroadTransferOrder.PostAbroadTransferOrderItemResponse;
import com.example.banking.services.client.order.OrderApi;
import com.example.banking.services.client.spAbroadTransferOrder.AbroadTransferOrderApi;
import com.example.banking.services.client.spAbroadTransferOrder.AbroadTransferOrderItemApi;
import com.example.banking.services.client.spAccount.AccountApi;
import com.example.banking.services.client.spApprovalEngineSheduledTask.ApprovalEngineScheduledTaskApi;
import com.example.banking.services.config.PropertiesConfiguration;
import com.example.banking.services.constants.MockData;
import com.example.banking.services.dto.EventTransferOrder;
import com.example.banking.services.enums.FormatDateEnum;
import com.example.banking.services.helper.error.SpAbroadTransferError;
import com.example.banking.services.service.AsyncAbroadTransferOrderService;
import com.example.banking.services.service.stream.Bridge;
import com.example.banking.services.utils.GenericUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AsyncAbroadTransferOrderServiceTest {

    @InjectMocks
    AsyncAbroadTransferOrderService asyncAbroadTransferOrderService;
    @Mock
    AbroadTransferOrderApi spAbroadTransferOrderApi;
    @Mock
    AbroadTransferOrderItemApi spAbroadTransferOrderItemApi;
    @Mock
    com.example.banking.services.client.bsAbroadTransferOrder.AbroadTransferOrderApi bsAbroadTransferOrderApi;
    @Mock
    com.example.banking.services.client.bsAbroadTransferOrder.AbroadTransferAuthorizationApi abroadTransferAuthorizationApi;
    @Mock
    PropertiesConfiguration propertiesConfiguration;
    @Mock
    ApprovalEngineScheduledTaskApi approvalEngineScheduledTaskApi;
    @Mock
    OrderApi orderApi;
    @Mock
    AccountApi accountApi;
    @Mock
    OrderHistoryApi orderHistoryApi;
    @Mock
    Bridge bridge;

    @BeforeEach
    void init() {
        when(propertiesConfiguration.getActionSpot())
                .thenReturn("TO_SPOT");
        when(propertiesConfiguration.getGenericUserPost())
                .thenReturn("TO_SPOT");
        when(propertiesConfiguration.getAppName())
                .thenReturn("");
        when(propertiesConfiguration.getDeviceIP())
                .thenReturn("");
        when(propertiesConfiguration.getAppName())
                .thenReturn("");
        when(propertiesConfiguration.getStateFinalApproval())
                .thenReturn("FINAL_APPROVAL");
        when(propertiesConfiguration.getExecutionTime())
                .thenReturn("9");
        when(propertiesConfiguration.getStateAvailable())
                .thenReturn("AVAILABLE");
        when(propertiesConfiguration.getActionApprove())
                .thenReturn("TO_APPROVAL");
        when(propertiesConfiguration.getStateEnabled())
                .thenReturn("ENABLED");
        when(propertiesConfiguration.getActionAvailable())
                .thenReturn("TO_AVAILABLE");
        when(propertiesConfiguration.getGenericUserPatch())
                .thenReturn("TO_AVAILABLE");
        when(propertiesConfiguration.getApplicationCode())
                .thenReturn("TO_AVAILABLE");
        when(propertiesConfiguration.getSpot())
                .thenReturn("SPOT");
        when(propertiesConfiguration.getCodeAgency())
                .thenReturn("SPOT");
        when(propertiesConfiguration.getAppNameReason())
                .thenReturn("SPOT");
        when(propertiesConfiguration.getStateSuccess())
                .thenReturn("SUCCESS");

        when(bsAbroadTransferOrderApi.getAbroadTransferOrderByFilter(anyMap(),any(),any()))
                .thenReturn(Mono.just(MockData.getBsAbroadTransferOrderByFilterResponse()));

        when(spAbroadTransferOrderApi.getAbroadTransferOrderByFilter(anyMap(),any()))
                .thenReturn(Mono.just(MockData.getSpAbroadTransferOrderByFilter()));

        when(spAbroadTransferOrderItemApi.patchAbroadTransferOrderItem(anyMap(),anyString(),any()))
                .thenReturn(Mono.just(new PostAbroadTransferOrderItemResponse()
                        .abroadTransferOrderItemId("123")));

        when(spAbroadTransferOrderApi.patchAbroadTransferOrder(anyMap(),anyString(),any()))
                .thenReturn(Mono.just(new AbroadTransferOrderResponse()
                        .abroadTransferOrderId("1234")));

        when(orderHistoryApi.getOrderHistoryByFilter(anyMap(),anyString()))
                .thenReturn(Mono.just(MockData.getOrderHistoryByFilter()));

        when(accountApi.getAccountById(anyMap(),anyString()))
                .thenReturn(Mono.just(MockData.getAccountById()));

        when(abroadTransferAuthorizationApi.postAbroadTransferExcute(anyMap(),any()))
                .thenReturn(Mono.empty());

        when(orderApi.patchOrder(anyMap(),any(),any()))
                .thenReturn(Mono.empty());

        when(approvalEngineScheduledTaskApi.postApprovalScheduledTask(anyMap(),any()))
                .thenReturn(Mono.empty());

        when(bridge.sendMessage(any()))
                .thenReturn(true);

    }

    @Test
    void shouldReturnSuccessWhenProcessTransactionIsOk() {
        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(MockData.evenetTransferOrder(), false))
                .verifyComplete();
    }

   @Test
    void  shouldReturnExeptionWhenGetAbroadTransferOrderByFilterIsRuntimeException() {

        when(spAbroadTransferOrderApi.getAbroadTransferOrderByFilter(anyMap(),any()))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(MockData.evenetTransferOrder(), false))
                .verifyErrorMatches(error -> error.equals(SpAbroadTransferError.FE02_SP_ABROAD_TRANSFER_ORDER));

    }

    @Test
    void shouldReturnSuccessWhenPatchOrderIsOk(){

        String currentDate=GenericUtil.convertDateToString(new Date(), FormatDateEnum.yyyy_MM_dd_T_HH_mm);
        GetAbroadTransferOrderByFilterResponse objGetAbroadTransferOrderByFilterResponse= MockData.getBsAbroadTransferOrderByFilterResponse();
        objGetAbroadTransferOrderByFilterResponse.getAbroadTransferOrder().setExecutionDate(currentDate);

        when(bsAbroadTransferOrderApi.getAbroadTransferOrderByFilter(anyMap(),any(),any()))
                .thenReturn(Mono.just(objGetAbroadTransferOrderByFilterResponse));

        GetOrderHistoryByFilterRetrieveResponse objGetOrderHistoryByFilterRetrieveResponse=MockData.getOrderHistoryByFilter();
        objGetOrderHistoryByFilterRetrieveResponse.getStatus().setCode("prueba");

        when(orderHistoryApi.getOrderHistoryByFilter(anyMap(),anyString()))
                .thenReturn(Mono.just(objGetOrderHistoryByFilterRetrieveResponse));

        when(propertiesConfiguration.getStateAvailable())
                .thenReturn("FINAL_APPROVAL");

        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(MockData.evenetTransferOrder(), false))
                .verifyComplete();
    }

    @Test
    void shouldReturnExeptionWhenPatchOrderIsRuntimeException() {

        String currentDate=GenericUtil.convertDateToString(new Date(),FormatDateEnum.yyyy_MM_dd_T_HH_mm);
        GetAbroadTransferOrderByFilterResponse objGetAbroadTransferOrderByFilterResponse= MockData.getBsAbroadTransferOrderByFilterResponse();
        objGetAbroadTransferOrderByFilterResponse.getAbroadTransferOrder().setExecutionDate(currentDate);

        when(bsAbroadTransferOrderApi.getAbroadTransferOrderByFilter(anyMap(),any(),any()))
                .thenReturn(Mono.just(objGetAbroadTransferOrderByFilterResponse));

        GetOrderHistoryByFilterRetrieveResponse objGetOrderHistoryByFilterRetrieveResponse=MockData.getOrderHistoryByFilter();
        objGetOrderHistoryByFilterRetrieveResponse.getStatus().setCode("prueba");

        when(orderHistoryApi.getOrderHistoryByFilter(anyMap(),anyString()))
                .thenReturn(Mono.just(objGetOrderHistoryByFilterRetrieveResponse));

        when(propertiesConfiguration.getStateAvailable())
                .thenReturn("FINAL_APPROVAL");

        when(orderApi.patchOrder(anyMap(),any(),any()))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(MockData.evenetTransferOrder(), true))
                .verifyErrorMatches(error -> error.equals(SpAbroadTransferError.FE05_BS_ORDER));

    }

    @Test
    void shouldReturnSuccessWhenPatchAbroadTransferAuthorizationIsOk(){

        String currentDate=GenericUtil.convertDateToString(new Date(),FormatDateEnum.yyyy_MM_dd_T_HH_mm);
        GetAbroadTransferOrderByFilterResponse objGetAbroadTransferOrderByFilterResponse= MockData.getBsAbroadTransferOrderByFilterResponse();
        objGetAbroadTransferOrderByFilterResponse.getAbroadTransferOrder().setExecutionDate(currentDate);

        when(bsAbroadTransferOrderApi.getAbroadTransferOrderByFilter(anyMap(),any(),any()))
                .thenReturn(Mono.just(objGetAbroadTransferOrderByFilterResponse));

        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(MockData.evenetTransferOrder(), false))
                .verifyComplete();
    }

    @Test
    void shouldReturnExeptionWhenPatchAbroadTransferOrderIsRuntimeException() {

        when(spAbroadTransferOrderApi.patchAbroadTransferOrder(anyMap(),anyString(),any()))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(MockData.evenetTransferOrder(), false))
                .verifyErrorMatches(error -> error.equals(SpAbroadTransferError.FE02_SP_ABROAD_TRANSFER_ORDER));

    }

    @Test
    void shouldReturnExeptionWhenPostApprovalScheduledTaskIsRuntimeException() {

        when(approvalEngineScheduledTaskApi.postApprovalScheduledTask(anyMap(),any()))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(MockData.evenetTransferOrder(), false))
                .verifyErrorMatches(error -> error.equals(SpAbroadTransferError.FE05_SP_TASK));

    }

    @Test
    void shouldReturnSuccessWhenFileIsSuccess() {
        String currentDate = GenericUtil.convertDateToString(new Date(), FormatDateEnum.yyyy_MM_dd_T_HH_mm);
        GetAbroadTransferOrderByFilterResponse objGetAbroadTransferOrderByFilterResponse = MockData.getBsAbroadTransferOrderByFilterResponse();
        objGetAbroadTransferOrderByFilterResponse.getAbroadTransferOrder().setExecutionDate(currentDate);
        objGetAbroadTransferOrderByFilterResponse.getAbroadTransferOrder().getAbroadTransferOrderItems()
                .get(0).getExemption().getFileExemption().getStatus().setCode("SUCCESS");

        when(bsAbroadTransferOrderApi.getAbroadTransferOrderByFilter(anyMap(), any(), any()))
                .thenReturn(Mono.just(objGetAbroadTransferOrderByFilterResponse));

        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(MockData.evenetTransferOrder(), false))
                .verifyComplete();
    }

    @Test
    void shouldReturnSuccessWhenPostOrderIsAvailable() {
        EventTransferOrder objEventTransferOrder=MockData.evenetTransferOrder();
        objEventTransferOrder.setNextStatus("AVAILABLE");
        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(objEventTransferOrder, false))
                .verifyComplete();
    }

    @Test
    void shouldReturnSuccessWhenPostOrderIsUnavailable(){

        String currentDate=GenericUtil.convertDateToString(new Date(),FormatDateEnum.yyyy_MM_dd_T_HH_mm);
        GetAbroadTransferOrderByFilterResponse objGetAbroadTransferOrderByFilterResponse= MockData.getBsAbroadTransferOrderByFilterResponse();
        objGetAbroadTransferOrderByFilterResponse.getAbroadTransferOrder().setExecutionDate(currentDate);
        objGetAbroadTransferOrderByFilterResponse.getAbroadTransferOrder().getAbroadTransferOrderItems()
                        .get(0).getExemption().setIsEnabled("UNANABLE");

        when(bsAbroadTransferOrderApi.getAbroadTransferOrderByFilter(anyMap(),any(),any()))
                .thenReturn(Mono.just(objGetAbroadTransferOrderByFilterResponse));

        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(MockData.evenetTransferOrder(), false))
                .verifyComplete();
    }

    @Test
    void shouldReturnExeptionWhenGetAbroadTransferOrderByFilterIsException() {

        when(bsAbroadTransferOrderApi.getAbroadTransferOrderByFilter(anyMap(),any(),any()))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(MockData.evenetTransferOrder(), false))
                .verifyErrorMatches(error -> error.equals(SpAbroadTransferError.FE04_BS_ABROAD_TRANSFER_ORDER));
    }

    @Test
    void shouldReturnExeptionWhenPostAbroadTransferExcuteIsException() {

        String currentDate=GenericUtil.convertDateToString(new Date(),FormatDateEnum.yyyy_MM_dd_T_HH_mm);
        GetAbroadTransferOrderByFilterResponse objGetAbroadTransferOrderByFilterResponse= MockData.getBsAbroadTransferOrderByFilterResponse();
        objGetAbroadTransferOrderByFilterResponse.getAbroadTransferOrder().setExecutionDate(currentDate);

        when(bsAbroadTransferOrderApi.getAbroadTransferOrderByFilter(anyMap(),any(),any()))
                .thenReturn(Mono.just(objGetAbroadTransferOrderByFilterResponse));

        when(abroadTransferAuthorizationApi.postAbroadTransferExcute(anyMap(),any()))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(MockData.evenetTransferOrder(), false))
                .verifyErrorMatches(error -> error.equals(SpAbroadTransferError.FE04_BS_ABROAD_TRANSFER_ORDER));

    }

    @Test
    void shouldReturnExeptionWhenGetAccountByIdIsException() {

        String currentDate=GenericUtil.convertDateToString(new Date(),FormatDateEnum.yyyy_MM_dd_T_HH_mm);
        GetAbroadTransferOrderByFilterResponse objGetAbroadTransferOrderByFilterResponse= MockData.getBsAbroadTransferOrderByFilterResponse();
        objGetAbroadTransferOrderByFilterResponse.getAbroadTransferOrder().setExecutionDate(currentDate);

        when(bsAbroadTransferOrderApi.getAbroadTransferOrderByFilter(anyMap(),any(),any()))
                .thenReturn(Mono.just(objGetAbroadTransferOrderByFilterResponse));

        when(accountApi.getAccountById(anyMap(),anyString()))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(MockData.evenetTransferOrder(), false))
                .verifyErrorMatches(error -> error.equals(SpAbroadTransferError.FE05_SP_ACCOUNT));

    }

    @Test
    void shouldReturnExeptionWhenBsGetOrderHistoryByFilterIsException() {

        String currentDate=GenericUtil.convertDateToString(new Date(),FormatDateEnum.yyyy_MM_dd_T_HH_mm);
        GetAbroadTransferOrderByFilterResponse objGetAbroadTransferOrderByFilterResponse= MockData.getBsAbroadTransferOrderByFilterResponse();
        objGetAbroadTransferOrderByFilterResponse.getAbroadTransferOrder().setExecutionDate(currentDate);

        when(bsAbroadTransferOrderApi.getAbroadTransferOrderByFilter(anyMap(),any(),any()))
                .thenReturn(Mono.just(objGetAbroadTransferOrderByFilterResponse));

        when(orderHistoryApi.getOrderHistoryByFilter(anyMap(),anyString()))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(MockData.evenetTransferOrder(), false))
                .verifyErrorMatches(error -> error.equals(SpAbroadTransferError.FE08_BS_ORDER_HISTORY));

    }

    @Test
    void shouldReturnExeptionWhenSpPatchAbroadTransferOrderItemIsException() {

        String currentDate=GenericUtil.convertDateToString(new Date(),FormatDateEnum.yyyy_MM_dd_T_HH_mm);
        GetAbroadTransferOrderByFilterResponse objGetAbroadTransferOrderByFilterResponse= MockData.getBsAbroadTransferOrderByFilterResponse();
        objGetAbroadTransferOrderByFilterResponse.getAbroadTransferOrder().setExecutionDate(currentDate);

        when(bsAbroadTransferOrderApi.getAbroadTransferOrderByFilter(anyMap(),any(),any()))
                .thenReturn(Mono.just(objGetAbroadTransferOrderByFilterResponse));

        when(spAbroadTransferOrderItemApi.patchAbroadTransferOrderItem(anyMap(),anyString(),any()))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(MockData.evenetTransferOrder(), false))
                .verifyErrorMatches(error -> error.equals(SpAbroadTransferError.FE02_SP_ABROAD_TRANSFER_ORDER));

    }

    @Test
    void shouldReturnExeptionWhenPatchOrderIsException(){

        String currentDate=GenericUtil.convertDateToString(new Date(),FormatDateEnum.yyyy_MM_dd_T_HH_mm);
        GetAbroadTransferOrderByFilterResponse objGetAbroadTransferOrderByFilterResponse= MockData.getBsAbroadTransferOrderByFilterResponse();
        objGetAbroadTransferOrderByFilterResponse.getAbroadTransferOrder().setExecutionDate(currentDate);

        when(bsAbroadTransferOrderApi.getAbroadTransferOrderByFilter(anyMap(),any(),any()))
                .thenReturn(Mono.just(objGetAbroadTransferOrderByFilterResponse));

        GetOrderHistoryByFilterRetrieveResponse objGetOrderHistoryByFilterRetrieveResponse=MockData.getOrderHistoryByFilter();
        objGetOrderHistoryByFilterRetrieveResponse.getStatus().setCode("prueba");

        when(orderHistoryApi.getOrderHistoryByFilter(anyMap(),anyString()))
                .thenReturn(Mono.just(objGetOrderHistoryByFilterRetrieveResponse));

        when(propertiesConfiguration.getStateAvailable())
                .thenReturn("FINAL_APPROVAL");

        when(orderApi.patchOrder(anyMap(),anyLong(),any()))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(asyncAbroadTransferOrderService.processTransaction(MockData.evenetTransferOrder(), false))
                .expectComplete();
    }
}