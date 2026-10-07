package com.example.banking.services.api.helper;

import com.example.banking.services.helper.HeaderSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {HeaderSupportTest.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class HeaderSupportTest {
  @Test
  void headers()
  {
    assertThat(HeaderSupport.postHeaderChannel("canal")).isNotNull();
    assertThat(HeaderSupport.postHeadersInterbankTransfer("affiliation","123")).isNotNull();
  }
}
