package com.example.banking.services.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HeaderSupportTest.class})
class HeaderSupportTest {


  @Test
  void getHeaders() {
    Map<String, String> headers = HeaderSupport.getHeaders("1", "2","3","4","5");
    assertThat(headers.size(), is(14));
  }
}