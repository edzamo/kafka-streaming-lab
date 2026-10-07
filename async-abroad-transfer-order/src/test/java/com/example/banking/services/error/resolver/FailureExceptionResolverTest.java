package com.example.banking.services.error.resolver;

import com.example.banking.services.helper.error.FailureException;
import com.example.banking.services.helper.error.SpAbroadTransferError;
import com.example.banking.services.helper.error.resolver.FailureExceptionResolver;
import com.example.banking.services.server.models.Error;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FailureExceptionResolverTest.class})
class FailureExceptionResolverTest {


  @Test
  void apply() {
    BindingResult bindingResult = Mockito.mock(BindingResult.class);
    List<ObjectError> errorsList = Collections.emptyList();
    Mockito.when(bindingResult.getAllErrors()).thenReturn(errorsList);

    MockServerHttpRequest request = MockServerHttpRequest.get("/")
        .header("Accept", "application/json")
        .build();
    ServerWebExchange exchange = MockServerWebExchange.from(request);

    WebExchangeBindException oba = new WebExchangeBindException(null, bindingResult);
    FailureException except = SpAbroadTransferError.FE01_UNEXPECTED_ERROR;
    FailureExceptionResolver obj = new FailureExceptionResolver();
    var exc = obj.apply(except);
    Error error = exc.apply(exchange);

    //005
    Assertions.assertSame("SP01", error.getErrorCode());
  }


}