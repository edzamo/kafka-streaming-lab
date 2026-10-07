package com.example.banking.services.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
@Getter
@Setter
@ToString
public class PropertiesConfiguration {
    @Value("${SEG_INTERVAL}")
    private Integer segInterval;
    @Value("${SEG_INTERVAL_LIMIT}")
    private Integer segIntervalLimit;
    @Value("${SEG_INTERVAL_INIT}")
    private Integer segIntervalInit;
    @Value("${ERROR_LAST_STEP}")
    private String errorLastStep;
    @Value("${STATUS_LAST_STEP}")
    private String statusLastStep;
    @Value("${USER_SYSTEM_DIRECT_TRANSFER}")
    private String userSystemDirectTransfer;
    @Value("${CLAIM_NAME}")
    private String claimName;
    @Value("${X_GUID}")
    private String xGuid;
    @Value("${X_DEVICE}")
    private String xDevice;
    @Value("${X_DEVICE_IP}")
    private String xDeviceIp;
    @Value("${X_SESSION}")
    private String xSession;
    @Value("${X_AGENCY}")
    private String xAgency;
    @Value("${X_GEOLOCATION}")
    private String xGeolocation;

    @Value("${info.project.name}")
    private String projectName;

    @Value("${app.name}")
    private String appName;

    @Value("${execute.order.process.code}")
    private String executionProcessCode;

    @Value("${modify.order.process.code}")
    private String modifyProcessCode;


    @Value("${default.header.x-device}")
    private String defaultHeaderDevice;


    @Value("${default.header.x-device-ip}")
    private String defaultHeaderDeviceIp;

}
