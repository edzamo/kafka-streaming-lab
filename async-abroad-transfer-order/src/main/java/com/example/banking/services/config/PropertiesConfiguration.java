package com.example.banking.services.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class PropertiesConfiguration {

    @Value("${CHANNEL}")
    private String channel;
    @Value("${DEVICE_IP}")
    private String deviceIP;
    @Value("${APP_NAME}")
    private String appName;
    @Value("${STATE_AVAILABLE}")
    private String stateAvailable;
    @Value("${STATE_FINAL_APPROVAL}")
    private String stateFinalApproval;
    @Value("${ACTION_APPROVE}")
    private String actionApprove;
    @Value("${STATE_ENABLED}")
    private String stateEnabled;
    @Value("${ACTION_AVAILABLE}")
    private String actionAvailable;
    @Value("${ACTION_PENDING}")
    private String actionPending;
    @Value("${ACTION_EXECUTE_INITIATE}")
    private String actionExecuteInitiate;
    @Value("${ACTION_EXECUTE_APPROVE}")
    private String actionExecuteApprove;
    @Value("${GENERIC_USER_PATCH}")
    private String genericUserPatch;
    @Value("${GENERIC_USER_POST}")
    private String genericUserPost;
    @Value("${ACTION_SPOT}")
    private String actionSpot;
    @Value("${EXECUTION_TIME}")
    private String executionTime;
    @Value("${SPOT}")
    private String spot;
    @Value("${CODE_AGENCY}")
    public String codeAgency;
    @Value("${APP_NAME_REASON}")
    public String appNameReason;
    @Value("${APPLICATION_CODE}")
    public String applicationCode;
    @Value("${STATE_SUCCESS}")
    public String stateSuccess;
    @Value("${STATE_PENDING}")
    public String statePending;
}
