package com.example.banking.services.helper.error.resolver;

import com.example.banking.services.helper.error.FailureException;
import com.example.banking.services.server.models.Error;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Function;

@Slf4j
public class FailureExceptionResolver implements
        Function<Throwable, Function<ServerWebExchange, Error>> {

    @Override
    public Function<ServerWebExchange, Error> apply(Throwable th) {
        FailureException ex = (FailureException) th;
        log.error("Failed by: {}", ((FailureException) th).getFailure());
        return exchange -> {
            exchange.getResponse().setRawStatusCode(ex.getErrorCode());
            return ex.getFailure();
        };
    }
}
