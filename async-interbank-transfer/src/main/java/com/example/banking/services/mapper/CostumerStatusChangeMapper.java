package com.example.banking.services.mapper;

import com.example.banking.services.client.models.bsInterbankTransferOrder.BsPostInterbankTransferRequest;
import com.example.banking.services.client.models.spInterbankTransferOrder.PostInterbankTransferUpdate;
import com.example.banking.services.dto.EventTransferOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface CostumerStatusChangeMapper {
    CostumerStatusChangeMapper INSTANCE = Mappers.getMapper(CostumerStatusChangeMapper.class);

    @Mapping(source = "nextStatus",target = "status.code")
    @Mapping(source = "orderId",target = "trayOrderId", qualifiedByName = "longToString")
    @Mapping(target = "sourceProduct", ignore = true)
    @Mapping(target = "uploadType.code", ignore = true)
    @Mapping(target = "executionDate", ignore = true)
    PostInterbankTransferUpdate eventTransferOrderToPatchInterbankTransferOrder (EventTransferOrder eventTransferOrder);

    @Mapping(target = "sourceProduct.productId",ignore = true)
    @Mapping(target = "amount",ignore = true)
    @Mapping(target = "currency.code",ignore = true)
    @Mapping(source = "orderId",target = "orderId")
    @Mapping(source = "action",target = "action.code")
    @Mapping(target = "destinationProduct.productId", ignore = true)
    @Mapping(target = "beneficiary.fullName", ignore = true)
    @Mapping(target = "beneficiary.email", ignore = true)
    @Mapping(target = "description", ignore = true)
    BsPostInterbankTransferRequest eventTransferOrderToPostDirectTransferOrderRequest (EventTransferOrder eventTransferOrder);


    @Mapping(source = "status",target = "status.code")
    @Mapping(source = "orderId",target = "trayOrderId", qualifiedByName = "longToString")
    @Mapping(target = "sourceProduct", ignore = true)
    @Mapping(target = "uploadType.code", ignore = true)
    @Mapping(source = "executionDate", target = "executionDate")
    PostInterbankTransferUpdate orderIdAndStatusAndExecutionDateToPatchInterbankTransferOrder(Long orderId, String status, OffsetDateTime executionDate);

    @Named("longToString")
    default String longToString(Long value)
    {
        return String.valueOf(value);
    }

}
