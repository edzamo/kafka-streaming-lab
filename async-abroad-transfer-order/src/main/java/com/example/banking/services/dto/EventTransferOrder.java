package com.example.banking.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventTransferOrder {
    private Long orderId;
    private String customerUserId;
    private String eventDate;
    private String nextStatus;
    private String channel;
    private String action;
    private String orderType;
    private String expirationDate;
    private String eventCode;
    private Integer segIntervalError;
}
