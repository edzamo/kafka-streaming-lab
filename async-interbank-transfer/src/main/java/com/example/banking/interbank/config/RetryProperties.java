package com.example.banking.interbank.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/** Retry waiting times, in seconds, and the output binding the retries are published on. */
@Validated
@ConfigurationProperties("interbank.retry")
record RetryProperties(int initialSeconds, int stepSeconds, int limitSeconds, @NotBlank String binding) {
}
