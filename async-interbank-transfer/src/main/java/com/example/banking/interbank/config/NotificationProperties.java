package com.example.banking.interbank.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("interbank.notification")
record NotificationProperties(@NotBlank String channel) {
}
