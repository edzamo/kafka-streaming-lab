package com.example.banking.services.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.banking.services.helper.error.ErrorResolverHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ErrorResolverHandlerTest.class})
class ErrorResolverHandlerTest {


    ObjectMapper mapper = new ObjectMapper();
    ErrorResolverHandler obj;

    @BeforeEach
    void setup() {
        obj = new ErrorResolverHandler(mapper);
    }

    @Test
    void afterPropertiesSet() {
        assertDoesNotThrow(obj::afterPropertiesSet);
    }

    @Test
    void handle() throws Exception {
        obj.afterPropertiesSet();
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
            .header("Accept", "application/json")
            .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        WebExchangeBindException oba = Mockito.mock(WebExchangeBindException.class);

        StepVerifier.create(obj.handle(exchange, oba))
            .expectComplete()
            .verify();
    }
}