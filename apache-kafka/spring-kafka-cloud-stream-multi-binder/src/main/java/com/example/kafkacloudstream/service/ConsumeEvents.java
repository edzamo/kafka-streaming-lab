
package com.example.kafkacloudstream.service;


import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConsumeEvents {

/*
  @Bean
  public Consumer<Message<String>> receive() {
    return this::processMessage;
  }

  public void processMessage(Message<String> message) {
    log.info("Receiving payment payload: {}", message.getPayload());
    log.info("Receiving payment header: {}", message.getHeaders());
  }*/

  @Bean
  public Consumer<Message<String>> consume() {
    return this::processMessageConsume;
  }

  public void processMessageConsume(Message<String> message) {
    log.info("Receiving queue report:" + message.getPayload());
  }


}
