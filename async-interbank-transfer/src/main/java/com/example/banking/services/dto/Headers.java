package com.example.banking.services.dto;

import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Generated
public class Headers {
    String appName;
    String requestId;
    String callerName;
    String processCode;
    String session;
    String device;
    String deviceIp;
    String language;
    String customerAffiliationId;
    String customerUserId;
}
