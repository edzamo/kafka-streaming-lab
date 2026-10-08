package com.example.banking.interbank.adapter.out.http;

import com.example.banking.interbank.application.in.RequestContext;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** The standard traceability headers of every downstream call, built once per call from the request context. */
record RequestHeaders(String appName, String callerName, String requestDate, String requestId, String session,
                      String device, String deviceIp, String language, String customerAffiliationId,
                      String customerUserId, String processCode) {

    private static final DateTimeFormatter REQUEST_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd;HH:mm:ss");

    static RequestHeaders from(RequestContext ctx, HeaderDefaults defaults, Clock clock) {
        return new RequestHeaders(
                orElse(ctx.appName(), defaults.appName()),
                defaults.callerName(),
                OffsetDateTime.now(clock).format(REQUEST_DATE),
                ctx.requestId(),
                orElse(ctx.session(), "def-" + UUID.randomUUID()),
                defaults.device(),
                defaults.deviceIp(),
                orElse(ctx.language(), "ES"),
                orElse(ctx.customerAffiliationId(), "def-cai-1111-2222-3333-44444"),
                orElse(ctx.customerUserId(), "def-cui-1111-2222-3333-44444"),
                ctx.processCode());
    }

    Map<String, String> asMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("app-name", appName);
        map.put("caller-name", callerName);
        map.put("request-date", requestDate);
        map.put("request-id", requestId);
        map.put("x-session", session);
        map.put("x-device", device);
        map.put("x-device-ip", deviceIp);
        map.put("x-language", language);
        map.put("customer-affiliation-id", customerAffiliationId);
        map.put("customer-user-id", customerUserId);
        map.put("process-code", processCode);
        return map;
    }

    private static String orElse(String value, String fallback) {
        return value != null ? value : fallback;
    }

    @Override
    public String toString() {
        return "RequestHeaders[requestId=" + requestId + ", processCode=" + processCode + "]";
    }
}
