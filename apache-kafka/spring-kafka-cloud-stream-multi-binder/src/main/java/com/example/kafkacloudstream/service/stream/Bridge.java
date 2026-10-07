package com.example.kafkacloudstream.service.stream;

import com.example.kafkacloudstream.domain.message.Payload;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class Bridge {

  private final StreamBridge streamBridge;
/*
  public void sendMessageReport(Object obj) {
    log.info("===> sendMessage ");
    streamBridge.send("report-out-0", obj);
  }*/

  public void sendMessagePayment(Payload payload) {
    Map<String, Object> headers = new HashMap<>();
    headers.put("app-name", "BackOffice");
    headers.put("caller-name", "sample-caller");
    headers.put("process-code", "PROC001");
    headers.put("request-date", "2019-04-29 07:20:50.52Z");
    headers.put("request-id", "id of request");
    headers.put("x-session", "a9fd8deb-2aad-4d88-a7e3-d153c9e44b66");
    headers.put("x-device", "9939aadd00ee32");
    headers.put("x-device-ip", "203.0.113.10");
    headers.put("x-language", "ES");
    headers.put("customer-affiliation-id", "qwerccf-a60b-45e8-b5e3-215924834e5");
    headers.put("customer-user-id", "qwerccf-a60b-45e8-b5e3-215924834e5");
    headers.put("x-guid", "550e8400-e29b-41d4-a716-446655440000");
    headers.put("x-channel", "10");
    headers.put("x-medium", "100002");
    headers.put("x-app", "00001");

    Message<Payload> message = MessageBuilder
        .withPayload(payload)
        .copyHeaders(headers)
        .build();
    log.info("===> sendMessage.... from POC  " );
    streamBridge.send("payment-out-0", message);
  }


}
