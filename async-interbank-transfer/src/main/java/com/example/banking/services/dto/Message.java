package com.example.banking.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import groovy.transform.ToString;
import lombok.*;

@ToString
@NoArgsConstructor
@Getter
@Builder
@AllArgsConstructor
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Generated
public class Message {
    Long trayOrderId;
}
