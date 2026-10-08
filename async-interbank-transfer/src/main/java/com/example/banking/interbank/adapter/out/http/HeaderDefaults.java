package com.example.banking.interbank.adapter.out.http;

/** Values the downstream APIs require that the incoming message may not carry. */
public record HeaderDefaults(String callerName, String appName, String device, String deviceIp,
                             String agency, String geolocation) {
}
