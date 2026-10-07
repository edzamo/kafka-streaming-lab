package com.example.banking.services.utils;

import com.example.banking.services.config.PropertiesConfiguration;
import com.example.banking.services.dto.Headers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HeaderUtils {

    public static final String HEADER_CALLER_NAME = "caller-name";
    public static final String HEADER_REQUEST_DATE = "request-date";
    public static final String HEADER_APP_NAME = "app-name";
    public static final String HEADER_PROCESS_CODE = "process-code";
    public static final String HEADER_REQUEST_ID = "request-id";
    public static final String HEADER_X_DEVICE = "x-device";
    public static final String HEADER_X_DEVICE_IP = "x-device-ip";
    public static final String HEADER_X_LANGUAGE = "x-language";
    public static final String HEADER_X_SESSION = "x-session";
    public static final String HEADER_CUSTOMER_USER_ID = "customer-user-id";
    public static final String HEADER_CUSTOMER_AFFILIATION_ID = "customer-affiliation-id";

    public final PropertiesConfiguration properties;


    public Map<String, String> buildHeadersMap(Headers headers) {
        return Map.ofEntries(
                new AbstractMap.SimpleEntry<>(HEADER_CALLER_NAME, properties.getProjectName()),
                new AbstractMap.SimpleEntry<>(HEADER_REQUEST_DATE, OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd;HH:mm:ss"))),
                new AbstractMap.SimpleEntry<>(HEADER_APP_NAME, headers.getAppName() != null ?
                        headers.getAppName() : properties.getAppName()),
                new AbstractMap.SimpleEntry<>(HEADER_REQUEST_ID, headers.getRequestId()),
                new AbstractMap.SimpleEntry<>(HEADER_X_SESSION, headers.getSession() != null ?
                        headers.getSession() : "def-" + UUID.randomUUID()),
                new AbstractMap.SimpleEntry<>(HEADER_PROCESS_CODE, headers.getProcessCode()),
                new AbstractMap.SimpleEntry<>(HEADER_X_DEVICE, headers.getDevice()),
                new AbstractMap.SimpleEntry<>(HEADER_X_DEVICE_IP, headers.getDeviceIp()),
                new AbstractMap.SimpleEntry<>(HEADER_X_LANGUAGE, headers.getLanguage() != null ? headers.getLanguage() : "ES"),
                new AbstractMap.SimpleEntry<>(HEADER_CUSTOMER_AFFILIATION_ID, headers.getCustomerAffiliationId() != null ?
                        headers.getCustomerAffiliationId() : "def-cai-1111-2222-3333-44444"),
                new AbstractMap.SimpleEntry<>(HEADER_CUSTOMER_USER_ID, headers.getCustomerUserId() != null ?
                        headers.getCustomerAffiliationId() : "def-cui-1111-2222-3333-44444")
        );
    }
}