package com.example.banking.services.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;


import lombok.RequiredArgsConstructor;

public class HeaderSupport {
    @Value("${LANGUAGE}")
    private static String language;

    public static Map<String, String> getHeaders(String appName,String callerName,String xDeviceIp,String customerAffiliationId,String customerUserId) {

        Map<String, String> headers = new HashMap<>();

        headers.put(HeaderConstant.ACCEPT, "application/json;charset=UTF-8");
        headers.put(HeaderConstant.CONTENT_TYPE, "application/json");
        headers.put(HeaderConstant.APP_NAME, appName);
        headers.put(HeaderConstant.CALLER_NAME, callerName);
        headers.put(HeaderConstant.X_DEVICE_IP, xDeviceIp);
        headers.put(HeaderConstant.CUSTOMER_AFFILIATION_ID, customerAffiliationId);
        headers.put(HeaderConstant.CUSTOMER_USER_ID, customerUserId);

        headers.put(HeaderConstant.REQUEST_ID, UUID.randomUUID().toString().replace("-", ""));
        headers.put(HeaderConstant.X_DEVICE, UUID.randomUUID().toString().replace("-", ""));
        headers.put(HeaderConstant.PROCESS_CODE, UUID.randomUUID().toString().replace("-", ""));
        headers.put(HeaderConstant.X_SESSION, UUID.randomUUID().toString().replace("-", ""));
        headers.put(HeaderConstant.X_LANGUAGE, language);
        headers.put(HeaderConstant.REQUEST_DATE, UUID.randomUUID().toString().replace("-", ""));

        headers.put(HeaderConstant.CHANNEL, appName);

        return headers;
    }
}
