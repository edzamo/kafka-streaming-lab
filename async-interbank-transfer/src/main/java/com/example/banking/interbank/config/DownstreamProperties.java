package com.example.banking.interbank.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/** Everything the outbound HTTP adapters need: API locations, call timeout and the default header values. */
@Validated
@ConfigurationProperties("interbank.downstream")
record DownstreamProperties(@NotNull Urls urls, @NotNull Duration timeout, @NotBlank String systemUserId,
                            @NotBlank String callerName, @NotBlank String appName, @NotBlank String device,
                            @NotBlank String deviceIp, @NotBlank String agency, @NotBlank String geolocation) {

    record Urls(@NotBlank String spInterbankTransferOrder, @NotBlank String bsInterbankTransferOrder,
                @NotBlank String bsOrder) {
    }
}
