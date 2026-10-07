package com.example.banking.services.service.stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class Bridge {
    @Autowired
    private StreamBridge streamBridge;
    public Boolean sendMessage(Object obj){
        return streamBridge.send("retryInterbank-out-0", obj);
    }
}
