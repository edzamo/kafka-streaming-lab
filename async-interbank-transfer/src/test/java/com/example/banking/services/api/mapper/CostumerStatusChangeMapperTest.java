package com.example.banking.services.api.mapper;

import com.example.banking.services.api.data.MockData;
import com.example.banking.services.client.models.bsInterbankTransferOrder.BsPostInterbankTransferRequest;
import com.example.banking.services.client.models.spInterbankTransferOrder.PostInterbankTransferUpdate;
import com.example.banking.services.mapper.CostumerStatusChangeMapper;
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
@ContextConfiguration(classes = {CostumerStatusChangeMapperTest.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class CostumerStatusChangeMapperTest {
  @Test
  void bsPostInterbankTransferOrderRequestToInterbankTransferOrderItemRequest() {
    PostInterbankTransferUpdate mapper= CostumerStatusChangeMapper.INSTANCE.eventTransferOrderToPatchInterbankTransferOrder(MockData.eventTransferOrder());
    assertThat(mapper.getTrayOrderId()).isEqualTo("114");
    assertThat(mapper.getStatus().getCode()).isEqualTo("PENDING_SEND");

  }


  @Test
  void bsPostTransferOrderRequestToTransferOrderItemRequest() {
    BsPostInterbankTransferRequest mapper= CostumerStatusChangeMapper.INSTANCE.eventTransferOrderToPostDirectTransferOrderRequest(MockData.eventTransferOrder());
    assertThat(mapper.getOrderId()).isEqualTo(114);
    assertThat(mapper.getAction().getCode()).isEqualTo("TO_PENDING");

    PostInterbankTransferUpdate mapper2= CostumerStatusChangeMapper.INSTANCE.eventTransferOrderToPatchInterbankTransferOrder(MockData.eventTransferOrder());
    assert mapper2.getStatus() != null;
    assertThat(mapper2.getStatus().getCode()).isEqualTo("PENDING_SEND");
    assertThat(mapper2.getTrayOrderId()).isEqualTo("114");
  }
}
