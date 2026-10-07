package com.example.banking.services.api.service.impl;

import com.example.banking.services.client.bsInterbankTransferOrder.InterbankTransferApi;
import com.example.banking.services.client.bsOrder.OrderApi;
import com.example.banking.services.client.models.bsInterbankTransferOrder.Currency;
import com.example.banking.services.client.models.bsInterbankTransferOrder.DestinationProduct;
import com.example.banking.services.client.models.bsInterbankTransferOrder.GetInterbankTransferOrder;
import com.example.banking.services.client.models.bsInterbankTransferOrder.OrderItem;
import com.example.banking.services.client.models.spInterbankTransferOrder.InterbankTransferOrderResponse;
import com.example.banking.services.config.PropertiesConfiguration;
import com.example.banking.services.dto.EventTransferOrder;
import com.example.banking.services.dto.Headers;
import com.example.banking.services.mapper.CostumerStatusChangeMapper;
import com.example.banking.services.service.AsyncInterbankTransferService;
import com.example.banking.services.service.stream.Bridge;
import com.example.banking.services.utils.HeaderUtils;
import com.example.banking.services.utils.JsonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.HashMap;

import static com.example.banking.services.api.data.MockData.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {AsyncInterbankTransferServiceTest.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class AsyncInterbankTransferServiceTest {

    @InjectMocks
    AsyncInterbankTransferService asyncInterbankTransferService;
    @Mock
    Bridge bridge;
    @Mock
    CostumerStatusChangeMapper costumerStatusChangeMapper;
    @Mock
    com.example.banking.services.client.spInterbankTransferOrder.InterbankTransferOrderApi spInterbankTransferOrderApi;
    @Mock
    com.example.banking.services.client.bsInterbankTransferOrder.InterbankTransferOrderApi bsInterbankTransferOrderApi;
    @Mock
    InterbankTransferApi interbankTransferApi;
    @Mock
    PropertiesConfiguration propertiesConfiguration;
    @Mock
    OrderApi orderApi;
    @Mock
    JsonSerializer jsonSerializer;
    @Mock
    HeaderUtils headerUtils;
    @Mock
    ReactiveStringRedisTemplate redisOperations;

    public static final String NEXT_STATUS = "FINAL_APPROVAL";

    @BeforeEach
    void setup() {
        when(headerUtils.buildHeadersMap(any())).thenReturn(new HashMap<>());
        when(propertiesConfiguration.getClaimName()).thenReturn("TRCTAPR");
    }

    @Test
    void shouldDoNothingWhenNextStatusIsPending() {
        EventTransferOrder eventTransferOrder = eventTransferOrder();

        eventTransferOrder.setNextStatus("PENDING");

        StepVerifier.create(asyncInterbankTransferService.processTransaction(
                        eventTransferOrder, Headers.builder().build()))
                .verifyComplete();

        verifyNoInteractions(orderApi);
        verifyNoInteractions(spInterbankTransferOrderApi);
        verifyNoInteractions(interbankTransferApi);
    }

    @Test
    void shouldUpdateStatusWhenNextStatusIsDifferentFromPending() {
        EventTransferOrder eventTransferOrder = eventTransferOrder();
        eventTransferOrder.setNextStatus("PENDING_APPROVAL");

        when(spInterbankTransferOrderApi.patchInterbankTransferOrder(anyMap(), any()))
                .thenReturn(Mono.just(new InterbankTransferOrderResponse()));

        when(redisOperations.convertAndSend(anyString(), anyString()))
                .thenReturn(Mono.just(1L));

        when(jsonSerializer.jsonObjectToString(any()))
                .thenReturn(getOrderMessageString());

        StepVerifier.create(asyncInterbankTransferService.processTransaction(
                        eventTransferOrder, Headers.builder().build()))
                .verifyComplete();

        verifyNoInteractions(orderApi);
        verify(spInterbankTransferOrderApi, times(1))
                .patchInterbankTransferOrder(anyMap(), any());

        verifyNoInteractions(interbankTransferApi);
    }

    @Test
    void shouldWriteIntoTheQueueIfUpdatingStatusFail() {
        EventTransferOrder eventTransferOrder = eventTransferOrder();
        eventTransferOrder.setNextStatus("PENDING_APPROVAL");

        when(spInterbankTransferOrderApi.patchInterbankTransferOrder(anyMap(), any()))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(asyncInterbankTransferService.processTransaction(eventTransferOrder, Headers.builder().build()))
                .verifyError();

        verify(spInterbankTransferOrderApi, times(1))
                .patchInterbankTransferOrder(anyMap(), any());

        verify(bridge, times(1))
                .sendMessage(any());
    }


    @Test
    void shouldExecuteDirectTransferIfNextStatusIsFinalApproval() {
        EventTransferOrder eventTransferOrder = eventTransferOrder();
        eventTransferOrder.setNextStatus(NEXT_STATUS);

        when(spInterbankTransferOrderApi.patchInterbankTransferOrder(anyMap(), any()))
                .thenReturn(Mono.just(new InterbankTransferOrderResponse()));

        when(redisOperations.convertAndSend(anyString(), anyString()))
                .thenReturn(Mono.just(1L));

        when(jsonSerializer.jsonObjectToString(any()))
                .thenReturn(getOrderMessageString());

        when(bsInterbankTransferOrderApi.getInterbankTransferOrderByFilter(anyMap(), anyInt(), anyString()))
                .thenReturn(Mono.just(getInterbankTransferOrder()));


        when(costumerStatusChangeMapper.eventTransferOrderToPostDirectTransferOrderRequest(any()))
                .thenReturn(postInterbankTransferOrderRequest());

        when(interbankTransferApi.postInterbankTransfer(anyMap(), any()))
                .thenReturn(Mono.empty());

        when(costumerStatusChangeMapper
                .orderIdAndStatusAndExecutionDateToPatchInterbankTransferOrder(anyLong(), anyString(), any()))
                .thenReturn(postInterbankTransferUpdate());

        when(orderApi.patchOrder(anyMap(), anyLong(), any())).thenReturn(Mono.empty());

        StepVerifier.create(asyncInterbankTransferService.processTransaction(eventTransferOrder, Headers.builder().build()))
                .verifyComplete();

        verify(interbankTransferApi, times(1)).postInterbankTransfer(anyMap(), any());

        verifyNoInteractions(bridge);
    }

    @Test
    void shouldWriteIntoTheQueueIfGetOrderFails() {
        EventTransferOrder eventTransferOrder = eventTransferOrder();
        eventTransferOrder.setNextStatus(NEXT_STATUS);

        when(redisOperations.convertAndSend(anyString(), anyString()))
                .thenReturn(Mono.just(1L));

        when(jsonSerializer.jsonObjectToString(any()))
                .thenReturn(getOrderMessageString());

        when(spInterbankTransferOrderApi.patchInterbankTransferOrder(anyMap(), any()))
                .thenReturn(Mono.just(new InterbankTransferOrderResponse()));

        when(bsInterbankTransferOrderApi.getInterbankTransferOrderByFilter(anyMap(), anyInt(), anyString()))
                .thenReturn(Mono.error(new RuntimeException()));


        StepVerifier.create(asyncInterbankTransferService.processTransaction(eventTransferOrder, Headers.builder().build()))
                .verifyError();

        verify(bridge, times(1)).sendMessage(any());
    }

    @Test
    void shouldWriteIntoTheQueueIfDirectTransferFails() {
        EventTransferOrder eventTransferOrder = eventTransferOrder();
        eventTransferOrder.setNextStatus(NEXT_STATUS);

        when(spInterbankTransferOrderApi.patchInterbankTransferOrder(anyMap(), any()))
                .thenReturn(Mono.just(new InterbankTransferOrderResponse()));

        when(bsInterbankTransferOrderApi.getInterbankTransferOrderByFilter(anyMap(), anyInt(), anyString()))
                .thenReturn(Mono.just(getInterbankTransferOrder()));

        when(redisOperations.convertAndSend(anyString(), anyString()))
                .thenReturn(Mono.just(1L));

        when(jsonSerializer.jsonObjectToString(any()))
                .thenReturn(getOrderMessageString());

        when(costumerStatusChangeMapper.eventTransferOrderToPostDirectTransferOrderRequest(any()))
                .thenReturn(postInterbankTransferOrderRequest());

        when(interbankTransferApi.postInterbankTransfer(anyMap(), any()))
                .thenReturn(Mono.error(new RuntimeException()));


        StepVerifier.create(asyncInterbankTransferService.processTransaction(eventTransferOrder, Headers.builder().build()))
                .verifyError();

        verify(bridge, times(1)).sendMessage(any());
    }

    @Test
    void shouldExecuteTwoDirectTransferIfOrderHasTwoItems() {
        when(redisOperations.convertAndSend(anyString(), anyString()))
                .thenReturn(Mono.just(1L));

        when(jsonSerializer.jsonObjectToString(any()))
                .thenReturn(getOrderMessageString());

        EventTransferOrder eventTransferOrder = eventTransferOrder();
        eventTransferOrder.setNextStatus(NEXT_STATUS);

        when(spInterbankTransferOrderApi.patchInterbankTransferOrder(anyMap(), any()))
                .thenReturn(Mono.just(new InterbankTransferOrderResponse()));

        GetInterbankTransferOrder order = getInterbankTransferOrder();
        assert order.getInterbankTransferOrderItems()!= null;
        order.getInterbankTransferOrderItems().add(new OrderItem()
                .amount(BigDecimal.TEN)
                .description("desc123")
                .currency(new Currency().code("USD"))
                .interbankTransferItemId("01910101")
                .destinationProduct(new DestinationProduct().productNumber("229290191")));

        when(bsInterbankTransferOrderApi.getInterbankTransferOrderByFilter(anyMap(), anyInt(), anyString()))
                .thenReturn(Mono.just(order));


        when(costumerStatusChangeMapper.eventTransferOrderToPostDirectTransferOrderRequest(any()))
                .thenReturn(postInterbankTransferOrderRequest());

        when(interbankTransferApi.postInterbankTransfer(anyMap(), any()))
                .thenReturn(Mono.empty());

        when(costumerStatusChangeMapper
                .orderIdAndStatusAndExecutionDateToPatchInterbankTransferOrder(anyLong(), anyString(), any()))
                .thenReturn(postInterbankTransferUpdate());

        when(orderApi.patchOrder(anyMap(), anyLong(), any())).thenReturn(Mono.empty());

        StepVerifier.create(asyncInterbankTransferService.processTransaction(eventTransferOrder, Headers.builder().build()))
                .verifyComplete();

        verify(interbankTransferApi, times(2)).postInterbankTransfer(anyMap(), any());

        verifyNoInteractions(bridge);
    }

    @Test
    void shouldUpdateStatusInProductDatabaseAndEngine() {

        when(spInterbankTransferOrderApi.patchInterbankTransferOrder(anyMap(), any()))
                .thenReturn(Mono.just(new InterbankTransferOrderResponse()));
        when(orderApi.patchOrder(anyMap(), anyLong(), any())).thenReturn(Mono.empty());

        when(redisOperations.convertAndSend(anyString(), anyString()))
                .thenReturn(Mono.just(1L));

        when(jsonSerializer.jsonObjectToString(any()))
                .thenReturn(getOrderMessageString());

        StepVerifier.create(asyncInterbankTransferService.processErrorPatchOrder(eventTransferOrder(), Headers.builder().build()))
                .verifyComplete();

        verify(spInterbankTransferOrderApi, times(1)).patchInterbankTransferOrder(anyMap(), any());
        verify(orderApi, times(1)).patchOrder(anyMap(), anyLong(), any());
    }

}
