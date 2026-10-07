package com.example.banking.services.api.configuration;

import com.example.banking.services.config.PropertiesConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PropertiesConfiguration.class)
@TestPropertySource(properties = {
        "SEG_INTERVAL=30",
        "SEG_INTERVAL_LIMIT=120",
        "SEG_INTERVAL_INIT=0",
        "ERROR_LAST_STEP=ERROR",
        "STATUS_LAST_STEP=SUCCESS",
        "USER_SYSTEM_DIRECT_TRANSFER=$PRODUCT_TRANSFER",
        "CLAIM_NAME=TRCTAPR",
        "X_GUID=fff",
        "X_DEVICE=san",
        "X_DEVICE_IP=local",
        "X_SESSION=ggg",
        "X_AGENCY=eeeee",
        "X_GEOLOCATION=dddd"
})
class PropertiesConfigurationTest {

    @Autowired
    PropertiesConfiguration propertiesConfiguration;

    @Test
    void shouldSegIntervalHaveTheCorrectValue(){
        assertEquals(30, propertiesConfiguration.getSegInterval());
    }

    @Test
    void shouldSegIntervalLimitHaveTheCorrectValue(){
        assertEquals(120, propertiesConfiguration.getSegIntervalLimit());
    }

    @Test
    void shouldSegIntervalInitHaveTheCorrectValue(){
        assertEquals(0, propertiesConfiguration.getSegIntervalInit());
    }

    @Test
    void shouldErrorLastStepHaveTheCorrectValue(){
        assertEquals("ERROR", propertiesConfiguration.getErrorLastStep());
    }

    @Test
    void shouldStatusLastStepHaveTheCorrectValue(){
        assertEquals("SUCCESS", propertiesConfiguration.getStatusLastStep());
    }

    @Test
    void shouldUserSystemDirectTransferHaveTheCorrectValue(){
        assertEquals("$PRODUCT_TRANSFER", propertiesConfiguration.getUserSystemDirectTransfer());
    }

    @Test
    void shouldClaimNameHaveTheCorrectValue(){
        assertEquals("TRCTAPR", propertiesConfiguration.getClaimName());
    }

    @Test
    void shouldApiGatewayPropertiesHaveTheCorrectValue(){
        assertEquals("fff", propertiesConfiguration.getXGuid());
        assertEquals("san", propertiesConfiguration.getXDevice());
        assertEquals("local", propertiesConfiguration.getXDeviceIp());
        assertEquals("ggg", propertiesConfiguration.getXSession());
        assertEquals("eeeee", propertiesConfiguration.getXAgency());
        assertEquals("dddd", propertiesConfiguration.getXGeolocation());
    }

}
