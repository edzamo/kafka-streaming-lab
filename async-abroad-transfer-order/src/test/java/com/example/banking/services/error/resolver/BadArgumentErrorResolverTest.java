package com.example.banking.services.error.resolver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
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
@ContextConfiguration(classes = {BadArgumentErrorResolverTest.class})
class BadArgumentErrorResolverTest {

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
    /*
    BadArgumentErrorResolver obj = new BadArgumentErrorResolver();
    var exch = obj.apply(oba);
    Error error = exch.apply(exchange);

    Assertions.assertSame("AFP400", error.getTitle());

     */
  }
}