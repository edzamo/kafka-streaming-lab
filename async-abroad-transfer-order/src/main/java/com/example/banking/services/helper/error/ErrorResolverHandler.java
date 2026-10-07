package com.example.banking.services.helper.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.banking.services.helper.error.resolver.FailureExceptionResolver;
import com.example.banking.services.helper.error.resolver.ResponseStatusExceptionResolver;
import com.example.banking.services.helper.error.resolver.UnexpectedErrorResolver;
import com.example.banking.services.server.models.Error;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Configuration
@Order(-2)
@RequiredArgsConstructor
public class ErrorResolverHandler implements ErrorWebExceptionHandler, InitializingBean {

    private Map<Class<? extends Throwable>,
            Function<Throwable, Function<ServerWebExchange, Error>>> errorResolver;
    private Function<Throwable, Function<ServerWebExchange, Error>> unhandledResolver;

    private final ObjectMapper mapper;

    @Override
    public void afterPropertiesSet() {
        log.info("Initializing error resolver");

        errorResolver = new HashMap<>();

        errorResolver.put(FailureException.class, new FailureExceptionResolver());
        errorResolver.put(ResponseStatusException.class, new ResponseStatusExceptionResolver());
        errorResolver.put(ServerWebInputException.class, new ResponseStatusExceptionResolver());
        errorResolver.put(WebExchangeBindException.class, new ResponseStatusExceptionResolver());
        unhandledResolver = new UnexpectedErrorResolver();
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();

        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return Mono.fromCallable(() -> errorResolver
                .getOrDefault(throwable.getClass(), unhandledResolver))
                .flatMap(fn -> exchange.getResponse()
                        .writeWith(Mono
                                .fromCallable(() -> mapper
                                        .writeValueAsBytes(fn.apply(throwable).apply(exchange)))
                                .map(bufferFactory::wrap)));
    }
}
