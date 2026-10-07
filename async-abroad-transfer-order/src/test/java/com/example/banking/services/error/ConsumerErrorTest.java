package com.example.banking.services.error;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ConsumerErrorTest.class})
class ConsumerErrorTest {

  @Test
  void getThrowableConsumer() {

    /*
    Mono.fromCallable(() ->
    {
      Error errorServer = new Error()
          .title("title")
          .detail("title")
          .instance("instance")
          .type("type")
          .resource("resource")
          .component("component")
          .backend("backend");
      throw new FailureException(errorServer, 400);
    }).doOnError(ConsumerError.getThrowableConsumer("string")).subscribe();

     */

  }

}