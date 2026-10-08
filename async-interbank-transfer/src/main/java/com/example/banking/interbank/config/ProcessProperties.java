package com.example.banking.interbank.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("interbank.process")
record ProcessProperties(@NotBlank String executeCode, @NotBlank String modifyCode, @NotBlank String errorStatus) {
}
