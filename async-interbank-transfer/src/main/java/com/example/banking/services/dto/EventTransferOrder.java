package com.example.banking.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import groovy.transform.ToString;
import lombok.*;

@ToString
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Generated
public class EventTransferOrder {
    Long orderId;
    String customerUserId;
    String eventDate;
    String status;
    String nextStatus;
    String channel;
    String action;
    Integer segIntervalError;
}
