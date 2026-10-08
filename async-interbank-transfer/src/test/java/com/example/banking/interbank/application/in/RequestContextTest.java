package com.example.banking.interbank.application.in;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestContextTest {

    private final RequestContext ctx = new RequestContext("req-1", null, "Banking", "session-secret-123", "ES",
            "affiliation-secret-456", "user-secret-789", "web", "APPROVE");

    @Test
    void should_mask_session_and_customer_ids_in_toString() {
        assertThat(ctx.toString())
                .contains("req-1", "Banking")
                .doesNotContain("session-secret-123", "affiliation-secret-456", "user-secret-789");
    }

    @Test
    void should_return_a_copy_with_the_process_code_and_keep_the_original_untouched() {
        RequestContext withCode = ctx.withProcessCode("INTERBANK04");

        assertThat(withCode.processCode()).isEqualTo("INTERBANK04");
        assertThat(withCode.requestId()).isEqualTo("req-1");
        assertThat(ctx.processCode()).isNull();
    }

    @Test
    void should_return_a_copy_with_the_customer_affiliation() {
        assertThat(ctx.withCustomerAffiliationId("other").customerAffiliationId()).isEqualTo("other");
        assertThat(ctx.customerAffiliationId()).isEqualTo("affiliation-secret-456");
    }
}
