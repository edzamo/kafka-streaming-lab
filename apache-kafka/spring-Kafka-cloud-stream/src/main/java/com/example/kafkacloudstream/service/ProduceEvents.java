package com.example.kafkacloudstream.service;

import com.example.kafkacloudstream.service.stream.Bridge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProduceEvents {

  private final Bridge bridge;

  public void sendPayment(String message) {
    bridge.sendMessagePayment(message);
  }



}
