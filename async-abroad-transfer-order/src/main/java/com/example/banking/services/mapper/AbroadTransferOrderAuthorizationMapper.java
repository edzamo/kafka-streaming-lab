package com.example.banking.services.mapper;

import com.example.banking.services.client.models.bsAbroadTransferOrder.*;
import org.springframework.util.ObjectUtils;

public class AbroadTransferOrderAuthorizationMapper {

    public static SupplementaryDataExecuteIntermediaryAccount mapperSupplementaryDataExecuteIntermediaryAccount(SupplementaryDataExecuteIntermediaryAccount intermediaryAccount) {
        //Default
        if (!ObjectUtils.isEmpty(intermediaryAccount)) {

            if (ObjectUtils.isEmpty(intermediaryAccount.getAccountId())) {
                intermediaryAccount.setAccountId("");
            }

            if (!ObjectUtils.isEmpty(intermediaryAccount.getFinancialEntity())) {

                if (ObjectUtils.isEmpty(intermediaryAccount.getFinancialEntity().getBic())) {
                    intermediaryAccount.getFinancialEntity().setBic("");
                }

                if (ObjectUtils.isEmpty(intermediaryAccount.getFinancialEntity().getName())) {
                    intermediaryAccount.getFinancialEntity().setName("");
                }

                if (!ObjectUtils.isEmpty(intermediaryAccount.getFinancialEntity().getAddress())) {

                    if (ObjectUtils.isEmpty(intermediaryAccount.getFinancialEntity().getAddress().getCountry())) {
                        if (ObjectUtils.isEmpty(intermediaryAccount.getFinancialEntity().getAddress().getCountry().getCode())) {
                            intermediaryAccount.getFinancialEntity().getAddress().getCountry().setCode("");
                        }

                        if (ObjectUtils.isEmpty(intermediaryAccount.getFinancialEntity().getAddress().getCountry().getDescription())) {
                            intermediaryAccount.getFinancialEntity().getAddress().getCountry().setDescription("");
                        }
                    }

                    if (ObjectUtils.isEmpty(intermediaryAccount.getFinancialEntity().getAddress().getCity())) {
                        if (ObjectUtils.isEmpty(intermediaryAccount.getFinancialEntity().getAddress().getCity().getDescription())) {
                            intermediaryAccount.getFinancialEntity().getAddress().getCity().setDescription("");
                        }
                    }

                }else{
                    intermediaryAccount.getFinancialEntity()
                            .setAddress(new SupplementaryDataExecuteIntermediaryAccountFinancialEntityAddress()
                                    .fullAddress("")
                                    .city(new SupplementaryDataExecuteIntermediaryAccountFinancialEntityAddressCity()
                                            .description(""))
                                    .country(new SupplementaryDataExecuteIntermediaryAccountFinancialEntityAddressCountry()
                                            .code("")
                                            .description("")));
                }

            }
        }else{
            return new SupplementaryDataExecuteIntermediaryAccount()
                    .accountId("")
                    .financialEntity(new SupplementaryDataExecuteIntermediaryAccountFinancialEntity()
                            .bic("")
                            .name("")
                            .address(new SupplementaryDataExecuteIntermediaryAccountFinancialEntityAddress()
                                    .fullAddress("")
                                    .city(new SupplementaryDataExecuteIntermediaryAccountFinancialEntityAddressCity()
                                            .description(""))
                                    .country(new SupplementaryDataExecuteIntermediaryAccountFinancialEntityAddressCountry()
                                            .code("")
                                            .description(""))));
        }
        return intermediaryAccount;
    }
}
