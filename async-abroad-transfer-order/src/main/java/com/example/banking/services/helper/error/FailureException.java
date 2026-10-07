package com.example.banking.services.helper.error;

import com.example.banking.services.server.models.Error;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class FailureException extends RuntimeException {

    private final Error failure;
    private final int errorCode;
}
