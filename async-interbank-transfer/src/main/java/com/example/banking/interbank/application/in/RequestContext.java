package com.example.banking.interbank.application.in;

/**
 * Traceability and caller data that travels with an order event through every downstream call.
 * Session and customer ids are masked in {@link #toString()}.
 */
public record RequestContext(String requestId, String processCode, String appName, String session, String language,
                             String customerAffiliationId, String customerUserId, String channel, String action) {

    public RequestContext withProcessCode(String newProcessCode) {
        return new RequestContext(requestId, newProcessCode, appName, session, language,
                customerAffiliationId, customerUserId, channel, action);
    }

    public RequestContext withCustomerAffiliationId(String newCustomerAffiliationId) {
        return new RequestContext(requestId, processCode, appName, session, language,
                newCustomerAffiliationId, customerUserId, channel, action);
    }

    public RequestContext withChannel(String newChannel) {
        return new RequestContext(requestId, processCode, appName, session, language,
                customerAffiliationId, customerUserId, newChannel, action);
    }

    public RequestContext withAction(String newAction) {
        return new RequestContext(requestId, processCode, appName, session, language,
                customerAffiliationId, customerUserId, channel, newAction);
    }

    @Override
    public String toString() {
        return "RequestContext[requestId=" + requestId + ", processCode=" + processCode + ", appName=" + appName
                + ", language=" + language + ", channel=" + channel + ", action=" + action
                + ", session=***, customerAffiliationId=***, customerUserId=***]";
    }
}
