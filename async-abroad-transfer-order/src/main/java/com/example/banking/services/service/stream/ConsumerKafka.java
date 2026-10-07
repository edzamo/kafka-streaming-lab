package com.example.banking.services.service.stream;

import com.example.banking.services.config.PropertiesConfiguration;
import com.example.banking.services.dto.EventTransferOrder;
import com.example.banking.services.service.AsyncAbroadTransferOrderService;
import io.cucumber.messages.internal.com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Consumer;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConsumerKafka {

    private final AsyncAbroadTransferOrderService asyncAbroadTransferOrderService;
    private final PropertiesConfiguration propertiesConfiguration;

    @Bean
    public Consumer<Message<String>> receive() {
        return this::processMessage;
    }

    public void processMessage(Message<String> message) {

        log.info("Receiving " + message.getPayload());

        EventTransferOrder eventTransferOrder;
        Gson gson = new Gson();
        eventTransferOrder = gson.fromJson(message.getPayload(), EventTransferOrder.class);

        //Si es TO_SPOT actualizar a estado TO_AVAILABLE
        if(propertiesConfiguration.getActionSpot().equalsIgnoreCase(eventTransferOrder.getAction())){
            eventTransferOrder.setNextStatus(propertiesConfiguration.getStateAvailable());
        }

        if(ObjectUtils.isEmpty(eventTransferOrder.getCustomerUserId()))
            eventTransferOrder.setCustomerUserId(UUID.randomUUID().toString());

        boolean isTask=false;
        if(!ObjectUtils.isEmpty(eventTransferOrder.getEventCode()))
            isTask=true;

        asyncAbroadTransferOrderService.processTransaction(eventTransferOrder, isTask)
                .subscribe();


    }
}
