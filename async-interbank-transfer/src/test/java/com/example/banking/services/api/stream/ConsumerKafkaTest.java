package com.example.banking.services.api.stream;

import com.google.gson.Gson;
import com.example.banking.services.api.data.MockData;
import com.example.banking.services.config.PropertiesConfiguration;
import com.example.banking.services.dto.EventTransferOrder;
import com.example.banking.services.helper.HeaderConstant;
import com.example.banking.services.service.AsyncInterbankTransferService;
import com.example.banking.services.service.stream.Bridge;
import com.example.banking.services.service.stream.ConsumerKafka;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;

@SpringBootTest
@ContextConfiguration(classes = {ConsumerKafkaTest.class})
class ConsumerKafkaTest {
    @Mock
    private Bridge bridgeMock = new Bridge();
    @InjectMocks
    ConsumerKafka consumerKafka;

    @Test
    void consumer() {
        var streamBridgeMock = mock(StreamBridge.class);
        doReturn(true).when(streamBridgeMock).send(Mockito.anyString(), Mockito.any(EventTransferOrder.class));
        EventTransferOrder order = MockData.eventTransferOrder();
        ReflectionTestUtils.setField(bridgeMock, "streamBridge", streamBridgeMock);
        bridgeMock.sendMessage(order);
        var consumer = mock(ConsumerKafka.class);
        consumer.receive();
        Mockito.verify(consumer).receive();
    }


    @Mock
    AsyncInterbankTransferService asyncInterbankTransferService;
    @Mock
    PropertiesConfiguration propertiesConfiguration;


    @BeforeEach
    void setup(){
        Mockito.when(propertiesConfiguration.getModifyProcessCode()).thenReturn("09209");
        Mockito.when(propertiesConfiguration.getExecutionProcessCode()).thenReturn("09209");
    }
    @Test
    void processMessage1() {
        EventTransferOrder order = mocks();
        Mockito.when(propertiesConfiguration.getSegIntervalLimit()).thenReturn(1);
        message(order);
    }

    @Test
    void processMessage2() {
        EventTransferOrder order = mocks();
        order.setSegIntervalError(null);
        Mockito.when(propertiesConfiguration.getSegIntervalLimit()).thenReturn(1);
        message(order);
    }

    @Test
    void processMessage3() {
        EventTransferOrder order = mocks();
        order.setSegIntervalError(null);
        Mockito.when(propertiesConfiguration.getSegIntervalLimit()).thenReturn(120);
        message(order);
    }

    @Test
    void processMessage4() {
        EventTransferOrder order = mocks();
        order.setSegIntervalError(120);
        Mockito.when(propertiesConfiguration.getSegIntervalLimit()).thenReturn(120);
        message(order);
    }

    EventTransferOrder mocks() {
        EventTransferOrder order = MockData.eventTransferOrder();
        Mockito.when(asyncInterbankTransferService.processTransaction(Mockito.any(EventTransferOrder.class), Mockito.any()))
                .thenReturn(Mono.empty());
        Mockito.when(asyncInterbankTransferService.processErrorPatchOrder(Mockito.any(EventTransferOrder.class), Mockito.any()))
                .thenReturn(Mono.empty());
        Mockito.when(propertiesConfiguration.getSegIntervalInit()).thenReturn(1);
        return order;
    }

}
