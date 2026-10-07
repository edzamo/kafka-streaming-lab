package com.example.banking.services.helper.error.resolver;

import com.example.banking.services.server.models.Error;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Function;

@Slf4j
public class UnexpectedErrorResolver implements
        Function<Throwable, Function<ServerWebExchange, Error>> {

    @Override
    public Function<ServerWebExchange, Error> apply(Throwable th) {
        String errorMessage = "Unexpected error";
        Error failure = new Error()
                .errorCode("AFP500")
                .userMessage(errorMessage)
                .developerMessage(errorMessage);

        return exchange -> {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return failure;
        };
    }
}
