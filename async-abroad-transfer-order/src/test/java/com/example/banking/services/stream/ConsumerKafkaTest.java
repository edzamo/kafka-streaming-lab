package com.example.banking.services.stream;

import com.example.banking.services.config.PropertiesConfiguration;
import com.example.banking.services.dto.EventTransferOrder;
import com.example.banking.services.helper.HeaderConstant;
import com.example.banking.services.service.AsyncAbroadTransferOrderService;
import com.example.banking.services.service.stream.ConsumerKafka;
import io.cucumber.messages.internal.com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;

import static com.example.banking.services.constants.MockData.evenetTransferOrder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ConsumerKafkaTest.class})
class ConsumerKafkaTest {

  @InjectMocks
  ConsumerKafka consumerKafka;
  @Mock
  AsyncAbroadTransferOrderService asyncAbroadTransferOrderService;
  @Mock
  PropertiesConfiguration propertiesConfiguration;

  @BeforeEach
  void init() {
    when(propertiesConfiguration.getActionSpot())
            .thenReturn("TO_SPOT");
  }

  @Test
  void processMessage1()
  {
    EventTransferOrder order= evenetTransferOrder();
    when(asyncAbroadTransferOrderService.processTransaction(any(), anyBoolean()))
            .thenReturn(Mono.empty());
    message(order);

  }

  @Test
  void processMessage2()
  {
    EventTransferOrder order= evenetTransferOrder();
    when(asyncAbroadTransferOrderService.processTransaction(any(), anyBoolean()))
            .thenReturn(Mono.empty());
    order.setCustomerUserId(null);
    order.setEventCode("prueba");
    order.setSegIntervalError(null);
    message(order);

  }

  void message(EventTransferOrder order)
  {
    Message<?> message = MessageBuilder.withPayload(new Gson().toJson(order)).build();
    consumerKafka.processMessage((Message<String>) message);
    assertThat(HeaderConstant.CHANNEL).isEqualTo("channel");
  }
}
