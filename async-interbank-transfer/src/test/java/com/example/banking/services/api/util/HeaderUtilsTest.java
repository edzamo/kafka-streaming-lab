package com.example.banking.services.api.util;


import com.example.banking.services.config.PropertiesConfiguration;
import com.example.banking.services.dto.Headers;
import com.example.banking.services.utils.HeaderUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class HeaderUtilsTest {


    @Mock
    PropertiesConfiguration properties;


    @InjectMocks
    HeaderUtils headerUtils;

    @BeforeEach
    void setup() {
        Mockito.when(properties.getProjectName()).thenReturn("98892");
    }

    @Test
    void shouldMessageQueueHeadersContainCorrectKeysEmptyHeaders() {

        Mockito.when(properties.getAppName()).thenReturn("98892");
        Map<String, String> headers = headerUtils.buildHeadersMap(Headers
                .builder()
                .requestId("1092")
                .processCode("1092")
                .device("1092")
                .deviceIp("1092")
                .build());
        assertHeaderKeys(headers);
    }

    @Test
    void shouldMessageQueueHeadersContainCorrectKeysWithValuesInHeaders() {

        Map<String, String> headers = headerUtils.buildHeadersMap(Headers
                .builder()
                .appName("092092")
                .requestId("1")
                .callerName("1")
                .processCode("1")
                .session("1")
                .device("1")
                .deviceIp("1")
                .language("1")
                .customerAffiliationId("1")
                .customerUserId("1")
                .build());
        assertHeaderKeys(headers);
    }


    void assertHeaderKeys(Map<String, String> headers) {
        assertTrue(headers.containsKey("caller-name"));
        assertTrue(headers.containsKey("request-date"));
        assertTrue(headers.containsKey("app-name"));
        assertTrue(headers.containsKey("request-id"));
        assertTrue(headers.containsKey("x-device"));
        assertTrue(headers.containsKey("x-device-ip"));
        assertTrue(headers.containsKey("x-session"));
        assertTrue(headers.containsKey("process-code"));
        assertTrue(headers.containsKey("x-language"));
        assertTrue(headers.containsKey("customer-user-id"));
        assertTrue(headers.containsKey("customer-affiliation-id"));
    }
}
