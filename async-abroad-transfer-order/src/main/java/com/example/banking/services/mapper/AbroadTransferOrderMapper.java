package com.example.banking.services.mapper;

import com.example.banking.services.client.models.bsAbroadTransferOrder.*;
import com.example.banking.services.client.models.order.PatchOrderRequest;
import com.example.banking.services.client.models.spAbroadTransferOrder.PatchAbroadTransferOrder;
import com.example.banking.services.client.models.spAbroadTransferOrder.PatchAbroadTransferOrderItem;
import com.example.banking.services.client.models.spAccount.AccountByIdResponse;
import com.example.banking.services.client.models.spApprovalEngineSheduledTask.PostApprovalScheduledTaskRequest;
import com.example.banking.services.dto.EventTransferOrder;
import com.example.banking.services.utils.GenericUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface AbroadTransferOrderMapper {

    AbroadTransferOrderMapper INSTANCE = Mappers.getMapper(AbroadTransferOrderMapper.class);

    @Mappings({
            @Mapping(source = "get.abroadTransferOrder.trayOrderId", target = "trayOrderId"),
            @Mapping(source = "actionExecute", target = "businessFlow"),
            //instructedAmount
            @Mapping(source = "get.abroadTransferOrder.amount", target = "instructedAmount.amount"),
            @Mapping(source = "get.abroadTransferOrder.sourceProduct.currency.code", target = "instructedAmount.currency.code"),
            //creditor Cuenta destino
            @Mapping(source = "item.destinationProduct.productNumber", target = "creditor.account.accountId"),
            @Mapping(target = "creditor.account.abaNumber",constant = ""),
            @Mapping(source = "item.beneficiary.documentNumber",target = "creditor.account.customer.documentNumber"),
            @Mapping(source = "item.beneficiary.documentType",target = "creditor.account.customer.documentType.code"),
            @Mapping(source = "item.beneficiary.fullName",target = "creditor.account.customer.fullName"),
            @Mapping(source = "item.beneficiary.address.summary",target = "creditor.account.customer.address.fullAddress"),
            @Mapping(source = "item.beneficiary.address.city",target = "creditor.account.customer.address.city.description"),
            @Mapping(source = "item.beneficiary.address.country.code",target = "creditor.account.customer.address.country.code"),
            @Mapping(source = "item.beneficiary.address.country.description",target = "creditor.account.customer.address.country.description"),
            @Mapping(target = "creditor.account.customer.address.zipCode",constant = ""),
            @Mapping(target = "creditor.account.product.code",constant = ""),
            @Mapping(source = "item.description", target = "creditor.account.referenceInformation"),
            @Mapping(source = "item.destinationProduct.financialEntity.financialEntityId",target = "creditor.account.financialEntity.bic"),
            @Mapping(source = "item.destinationProduct.financialEntity.fullName",target = "creditor.account.financialEntity.name"),
            @Mapping(source = "item.destinationProduct.financialEntity.address.summary",target = "creditor.account.financialEntity.address.fullAddress"),
            @Mapping(source = "item.destinationProduct.financialEntity.address.city",target = "creditor.account.financialEntity.address.city.description"),
            @Mapping(source = "item.destinationProduct.financialEntity.address.country.code",target = "creditor.account.financialEntity.address.country.code"),
            @Mapping(source = "item.destinationProduct.financialEntity.address.country.description",target = "creditor.account.financialEntity.address.country.description"),
            //debtor Cuenta origen
            @Mapping(source = "get.abroadTransferOrder.sourceProduct.productNumber", target = "debtor.account.accountId"),
            @Mapping(source = "origen.customer.documentNumber",target = "debtor.account.customer.documentNumber"),
            @Mapping(source = "origen.customer.documentType",target = "debtor.account.customer.documentType.code"),
            @Mapping(source = "origen.customer.fullname", target = "debtor.account.customer.fullName"),
            @Mapping(target = "debtor.account.customer.address.fullAddress",constant = "Ecuador"),
            @Mapping(target = "debtor.account.customer.address.city.description",constant = ""),
            @Mapping(target = "debtor.account.customer.address.country.code",constant = ""),
            @Mapping(target = "debtor.account.customer.address.country.description",constant = ""),
            @Mapping(target = "debtor.account.customer.address.zipCode",constant = ""),
            @Mapping(target = "debtor.account.segments.structuralSegments.code",constant = ""),
            @Mapping(target = "debtor.account.segments.structuralSegments.subStructuralSegments.code",constant = ""),
            @Mapping(source = "origen.balanceInformation.availableAmount", target = "debtor.account.balanceInformation.availableBalance.amount"),//Valor disponible de la cuenta
            @Mapping(source = "origen.product.code", target = "debtor.account.product.code"),
            @Mapping(source = "origen.type.description", target = "debtor.account.product.description"),
            @Mapping(source = "origen.subProduct.code", target = "debtor.account.product.subProduct.code"),
            //supplementaryData objeto vacio por default
            @Mapping(target = "supplementaryData.supplementaryData.intermediaryAccount.accountId",constant = ""),
            @Mapping(source = "item.intermediaryFinancialEntity.intermediaryFinancialEntityId", target = "supplementaryData.intermediaryAccount.financialEntity.bic"),
            @Mapping(source = "item.intermediaryFinancialEntity.fullName", target = "supplementaryData.intermediaryAccount.financialEntity.name"),
            @Mapping(source = "item.intermediaryFinancialEntity.address.summary", target = "supplementaryData.intermediaryAccount.financialEntity.address.fullAddress"),
            @Mapping(source = "item.intermediaryFinancialEntity.address.city", target = "supplementaryData.intermediaryAccount.financialEntity.address.city.description"),
            @Mapping(source = "item.intermediaryFinancialEntity.address.country.code",target = "supplementaryData.intermediaryAccount.financialEntity.address.country.code"),
            @Mapping(source = "item.intermediaryFinancialEntity.address.country.description",target = "supplementaryData.intermediaryAccount.financialEntity.address.country.description"),
            //creditorInstructedAmount
            @Mapping(source = "item.amount",target = "supplementaryData.creditorInstructedAmount.amount"),
            @Mapping(source = "item.currency.code",target = "supplementaryData.creditorInstructedAmount.currency.code"),
            //isd
            @Mapping(source = "isExempt",target = "supplementaryData.isd.isExempt"),
            @Mapping(target = "supplementaryData.isd.exemptionCode",constant = "02"),
            @Mapping(target = "supplementaryData.isd.receivedForm",constant = "N"),
            @Mapping(source = "item.exemption.amount",target = "supplementaryData.isd.exemptionAmount"),
            @Mapping(source = "item.exemption.quotaAvailable",target = "supplementaryData.isd.availableQuotaAmount"),
            @Mapping(source = "item.isd.percentage",target = "supplementaryData.isd.percentage"),
            @Mapping(source=  "item.exemption.type.code", target = "supplementaryData.isd.code"),
            @Mapping(source = "operation", target = "supplementaryData.isd.description", qualifiedByName = "bbIsdCobro"),
            @Mapping( source = "item.isd.value",target = "supplementaryData.isd.amount"),
            @Mapping(target = "supplementaryData.isd.exemptionForm",constant = ""),
            //transaction
            @Mapping(source = "operation", target = "supplementaryData.transaction.amountDescription", qualifiedByName = "montoGirosExterior"),
            @Mapping(source = "item.fee.value", target = "supplementaryData.transaction.comissionFee.amount",qualifiedByName = "round",defaultValue = "0.00"),
            @Mapping(source = "operation", target = "supplementaryData.transaction.comissionFee.description", qualifiedByName = "comisionGirosExterior"),
            @Mapping(source = "item.remittanceCostType.code",target = "supplementaryData.transaction.comissionFee.type.code"),
            @Mapping(source="operation", target = "supplementaryData.transaction.vat.description", qualifiedByName = "ivaComisuinGirosExt"),
            //supplementaryReasonData catalogo
            @Mapping(target = "supplementaryData.supplementaryReasonData.code",constant = ""),
            @Mapping(target = "supplementaryData.supplementaryReasonData.bceCredit.code",constant = ""),
            @Mapping(target = "supplementaryData.supplementaryReasonData.bceCredit.date",constant = ""),
            @Mapping(target = "supplementaryData.supplementaryReasonData.dauRecord",constant = ""),
            @Mapping(target = "supplementaryData.supplementaryReasonData.reinsuranceContractNumber",constant = ""),
            //messageChannelDetail
            @Mapping(target = "supplementaryData.messageChannelDetail.catalog.catalogId",constant = "codigoAgencia"),
            @Mapping(source = "codeAgency", target ="supplementaryData.messageChannelDetail.catalog.code"),
            //swiftMessage
            @Mapping(target = "supplementaryData.swiftMessage.type.subType.code",constant = "1"),
            @Mapping(target = "supplementaryData.swiftMessage.code",constant = "103"),
            @Mapping(source = "operation",target = "supplementaryData.swiftMessage.operation.code"),
            @Mapping(target = "supplementaryData.swiftMessage.generateUserKey",constant = "false"),
            @Mapping(source = "userKey",target = "supplementaryData.swiftMessage.userKey"),
            @Mapping(source = "userGeneric",target = "supplementaryData.swiftMessage.sessionUser",qualifiedByName = "removeSpecialCharacters"),
            @Mapping(source = "orderHistoryApprove.customerUser.names",target = "supplementaryData.swiftMessage.verificationUser"),
            @Mapping(source = "applicationCode", target = "supplementaryData.swiftMessage.application.code"),
            @Mapping(source = "appNameReason", target = "supplementaryData.swiftMessage.application.parameter.code"),
            @Mapping(target = "supplementaryData.swiftMessage.application.type.subType.code",constant = "1"),
            @Mapping(source = "item.intermediaryFinancialEntity.intermediaryFinancialEntityId",target = "supplementaryData.swiftMessage.reciever.code"),
            @Mapping(target = "supplementaryData.swiftMessage.application.service.code",constant = ""),
            @Mapping(target = "supplementaryData.swiftMessage.approvalStatus.code",constant = "BA"),
            @Mapping(target = "supplementaryData.swiftMessage.dcStatus.code",constant = "PEN"),
            @Mapping(target = "supplementaryData.swiftMessage.isFinancial",constant = "true"),
            @Mapping(source = "listSupplementaryDataExecuteAdditionalFields",target = "supplementaryData.additionalFields"),
            //exchangeRateInformation
            @Mapping(target = "exchangeRateInformation.exchangeRate",constant = "1.00000")
    })
    PostAbroadTransferExcuteRequest bsAbroadTransferOrderToAbroadTransferExcute(String userKey, GetAbroadTransferOrderByFilterResponse get, AbroadTransferOrderItemGet item, AccountByIdResponse origen, OrderHistory orderHistoryApprove, String userGeneric, String operation, String codeAgency, String appNameReason, String applicationCode, String actionExecute, List<SupplementaryDataExecuteAdditionalFields> listSupplementaryDataExecuteAdditionalFields, String isExempt);

    @Named("isExempt")
    static String isExempt(String isExempt) {
        return "ENABLE".equalsIgnoreCase(isExempt)?"S":"N";
    }

    @Named("ivaComisuinGirosExt")
    static String ivaComisuinGirosExt(String operation) {

        return "IVA COMISION "+operation;
    }

    @Named("montoGirosExterior")
    static String montoGirosExterior(String operation) {

        return "MNT GIRO EXT "+operation;
    }

    @Named("comisionGirosExterior")
    static String comisionGirosExterior(String operation) {

        return "COM GIRO EXT "+operation;
    }

    @Named("bbIsdCobro")
    static String bbIsdCobro(String operation) {

        return "BB ISD-COBRO "+operation;
    }



    @Named("round")
    public static String round(String num) {
        double random = Double.parseDouble(num);
        BigDecimal bd =  BigDecimal.valueOf(random);
        bd = bd.setScale(2, RoundingMode.HALF_UP); //
        return String.valueOf(bd);
    }

    @Named("removeSpecialCharacters")
    static String removeSpecialCharacters(String value) {
        return GenericUtil.removeSpecialCharacters(value);
    }

    @Mapping(source = "trayOrderId",target = "trayOrderId")
    @Mapping(source = "status",target = "status.code")
    @Mapping(source = "fxReferenceId",target = "fxReferenceId")
    PatchAbroadTransferOrder eventTransferOrderToSpPatchAbroadTransferOrder(String trayOrderId,String status,String fxReferenceId);

    @Mapping(source = "spot",target = "currencyExchange.operationType")
    PatchAbroadTransferOrderItem eventTransferOrderToSpPatchAbroadTransferOrderItem(String spot);

    @Mapping(source = "genericUserPatch",target = "customerUser.customerUserId")
    @Mapping(source = "actionAvailable",target = "action.code")
    PatchOrderRequest eventTransferOrderToPatchOrderRequest(String genericUserPatch, String actionAvailable);

    @Mapping(source = "plusHours",target = "sheduledDate")
    @Mapping(source = "eventTransferOrder.orderId",target = "order.orderId")
    @Mapping(source = "eventTransferOrder.orderType",target = "order.product.name")
    @Mapping(source = "eventTransferOrder.nextStatus",target = "order.status.code")
    @Mapping(source = "eventTransferOrder.action",target = "order.action.code")
    PostApprovalScheduledTaskRequest eventTransferOrderToPostApprovalScheduledTaskRequest(OffsetDateTime plusHours, EventTransferOrder eventTransferOrder);
}
