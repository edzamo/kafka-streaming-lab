package com.example.banking.services.api.stream;

import com.example.banking.services.api.data.MockData;
import com.example.banking.services.dto.EventTransferOrder;
import com.example.banking.services.service.stream.Bridge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ContextConfiguration(classes = {BridgeTest.class})
class BridgeTest {
  @InjectMocks
  private Bridge bridgeMock= new Bridge();
  @Test
  void sendMessage(){
    var streamBridgeMock = mock(StreamBridge.class);
    doReturn(true).when(streamBridgeMock).send(Mockito.anyString(),Mockito.any(EventTransferOrder.class));
    EventTransferOrder order= MockData.eventTransferOrder();
    ReflectionTestUtils.setField(bridgeMock, "streamBridge", streamBridgeMock);
    Assertions.assertEquals(true, bridgeMock.sendMessage(order));
  }
}
