package com.example.banking.services.helper;

import java.util.HashMap;
import java.util.Map;

public class HeaderSupport {
    private HeaderSupport() {
        // Do nothing because of X and Y.
    }
    
    public static Map<String, String> postHeadersInterbankTransfer(String customerAffiliationId,String trayOrderId) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HeaderConstant.CUSTOMER_AFFILIATION_ID, customerAffiliationId);
        headers.put(HeaderConstant.TRAY_ORDER_ID, trayOrderId);
        return headers;
    }

    public static Map<String, String> postHeaderChannel(String channel) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HeaderConstant.CHANNEL, channel);
        return headers;
    }
}
