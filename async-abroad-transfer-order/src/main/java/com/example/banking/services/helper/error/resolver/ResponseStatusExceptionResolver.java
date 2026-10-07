package com.example.banking.services.helper.error.resolver;

import com.example.banking.services.server.models.Error;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Function;

@Slf4j
public class ResponseStatusExceptionResolver implements Function<Throwable, Function<ServerWebExchange, Error>> {

    @Override
    public Function<ServerWebExchange, Error> apply(Throwable th) {
        ResponseStatusException ex = (ResponseStatusException) th;
        return exchange -> {
            exchange.getResponse().setRawStatusCode(ex.getStatus().value());
            return new Error().errorCode(String.valueOf(ex.getStatus().value()))
                    .userMessage(ex.getMessage())
                    .developerMessage(ex.getReason());
        };
    }
}
