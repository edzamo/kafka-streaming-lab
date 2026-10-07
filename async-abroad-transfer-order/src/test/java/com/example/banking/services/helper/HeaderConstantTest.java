package com.example.banking.services.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HeaderConstantTest.class})
class HeaderConstantTest {


  @Test
  void getHeaders() {
    assertEquals(HeaderConstant.X_API_KEY_CREDENTIALS, "X-Gateway-Credentials");
    assertEquals(HeaderConstant.X_DEVICE_IP, "X-Device-IP");
    assertEquals(HeaderConstant.X_GUID, "X-Guid");
    assertEquals(HeaderConstant.X_DEVICE, "X-Device");
    assertEquals(HeaderConstant.X_SESSION, "X-Session");

  }
}