package com.example.banking.services.service.stream;

import com.google.gson.Gson;
import com.example.banking.services.config.PropertiesConfiguration;
import com.example.banking.services.dto.EventTransferOrder;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import com.example.banking.services.dto.Headers;
import com.example.banking.services.helper.Constants;
import com.example.banking.services.service.AsyncInterbankTransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import reactor.util.context.Context;

import static com.example.banking.services.utils.HeaderUtils.*;

@Component
@Slf4j
public class ConsumerKafka {
  @Autowired
  AsyncInterbankTransferService asyncInterbankTransferService;
  @Autowired
  PropertiesConfiguration propertiesConfiguration;

  @Bean
  public Consumer<Message<String>> receive() {
    return this::processMessage;
  }
  public void processMessage(Message<String> message)
  {
    log.info("New message received");

    log.debug("Headers received " + message.getHeaders());
    EventTransferOrder eventTransferOrder;
    Gson gson = new Gson();
    eventTransferOrder = gson.fromJson(message.getPayload(), EventTransferOrder.class);

    log.info("Processing order " + eventTransferOrder.getOrderId() + ", action " + eventTransferOrder.getAction());
    log.debug("Gson parsing :" + eventTransferOrder);

    Headers headers = buildHeaders(message.getHeaders(), eventTransferOrder.getNextStatus());
    Context context = buildContext(headers.getRequestId(), headers.getProcessCode());
    //Processor transaction
    if (eventTransferOrder.getSegIntervalError() == null)
      eventTransferOrder.setSegIntervalError(propertiesConfiguration.getSegIntervalInit());
    if (eventTransferOrder.getSegIntervalError() < propertiesConfiguration.getSegIntervalLimit()) {
      secondsWait(eventTransferOrder.getSegIntervalError());
      asyncInterbankTransferService.processTransaction(eventTransferOrder, headers)
              .contextWrite(context)
              .subscribe();
    }
    if (eventTransferOrder.getSegIntervalError() >= propertiesConfiguration.getSegIntervalLimit())
      asyncInterbankTransferService.processErrorPatchOrder(eventTransferOrder, headers)
              .contextWrite(context)
              .subscribe();
  }

  private Context buildContext(String requestId, String processCode) {
    Map<String, String> contextMDC = Map.of(
            HEADER_REQUEST_ID, requestId,
            HEADER_PROCESS_CODE, processCode
    );

    return Context.of("MDC_CTX", contextMDC);
  }

  private Headers buildHeaders(MessageHeaders headers, String nextStatus) {


    String processCode;
    if (Objects.equals(Constants.FINAL_APPROVAL, nextStatus)) {
      processCode = propertiesConfiguration.getExecutionProcessCode();
    } else {
      processCode = propertiesConfiguration.getModifyProcessCode();
    }
    return Headers.builder()
            .appName(objectToString(headers.get(HEADER_APP_NAME)))
            .requestId(objectToString(headers.get(HEADER_REQUEST_ID), "def-" + UUID.randomUUID()))
            .session(objectToString(headers.get(HEADER_X_SESSION)))
            .device(propertiesConfiguration.getDefaultHeaderDevice())
            .deviceIp(propertiesConfiguration.getDefaultHeaderDeviceIp())
            .language(objectToString(HEADER_X_LANGUAGE))
            .processCode(processCode)
            .customerAffiliationId(objectToString(HEADER_CUSTOMER_AFFILIATION_ID))
            .customerUserId(objectToString(HEADER_CUSTOMER_USER_ID))
            .build();

  }

  private static String objectToString(Object object, String defaultValue) {
    return Objects.toString(object, defaultValue);
  }
  private static String objectToString(Object object) {
    return objectToString(object, null);
  }

  private static void secondsWait(int segundosIntervalError){
    try {
      Thread.sleep(segundosIntervalError * 1000L);
    } catch (InterruptedException ie) {
      log.error("InterruptedException: ", ie);
      Thread.currentThread().interrupt();
    }
  }
}
