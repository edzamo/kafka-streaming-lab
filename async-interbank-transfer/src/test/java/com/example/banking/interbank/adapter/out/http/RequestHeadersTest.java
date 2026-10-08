package com.example.banking.interbank.adapter.out.http;

import com.example.banking.interbank.application.in.RequestContext;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RequestHeadersTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-03-04T05:06:07Z"), ZoneOffset.UTC);
    private final HeaderDefaults defaults =
            new HeaderDefaults("async-interbank-transfer", "DefaultApp", "dev-1", "10.0.0.1", "agency-1", "geo-1");
    private final RequestContext full = new RequestContext("req-1", "INTERBANK04", "Banking", "session-1", "EN",
            "aff-1", "user-1", "web", "APPROVE");

    @Test
    void should_carry_each_context_value_in_its_own_header_and_use_the_customer_user_id_for_customer_user_id() {
        var headers = RequestHeaders.from(full, defaults, clock);

        assertThat(headers.asMap()).containsEntry("app-name", "Banking")
                .containsEntry("request-id", "req-1")
                .containsEntry("process-code", "INTERBANK04")
                .containsEntry("x-session", "session-1")
                .containsEntry("x-language", "EN")
                .containsEntry("customer-affiliation-id", "aff-1")
                .containsEntry("customer-user-id", "user-1");
    }

    @Test
    void should_take_caller_device_and_request_date_from_the_defaults_and_the_clock() {
        var headers = RequestHeaders.from(full, defaults, clock);

        assertThat(headers.asMap()).containsEntry("caller-name", "async-interbank-transfer")
                .containsEntry("x-device", "dev-1")
                .containsEntry("x-device-ip", "10.0.0.1")
                .containsEntry("request-date", "2026-03-04;05:06:07");
    }

    @Test
    void should_fall_back_to_defaults_for_the_values_the_message_did_not_carry() {
        var empty = new RequestContext("req-1", "INTERBANK02", null, null, null, null, null, null, null);

        var map = RequestHeaders.from(empty, defaults, clock).asMap();

        assertThat(map).containsEntry("app-name", "DefaultApp").containsEntry("x-language", "ES")
                .containsEntry("customer-affiliation-id", "def-cai-1111-2222-3333-44444")
                .containsEntry("customer-user-id", "def-cui-1111-2222-3333-44444");
        assertThat(map.get("x-session")).startsWith("def-");
    }

    @Test
    void should_not_expose_session_or_customer_ids_in_toString() {
        assertThat(RequestHeaders.from(full, defaults, clock).toString())
                .doesNotContain("session-1", "aff-1", "user-1");
    }
}
