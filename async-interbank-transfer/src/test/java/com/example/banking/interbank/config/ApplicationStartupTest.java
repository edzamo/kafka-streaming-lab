package com.example.banking.interbank.config;

import com.example.banking.interbank.InterbankTransferApplication;
import com.example.banking.interbank.adapter.out.messaging.StreamBridgeOrderEventPublisher;
import com.example.banking.interbank.adapter.out.redis.RedisOrderChangeNotifier;
import com.example.banking.interbank.application.in.HandleTransferOrderEvent;
import com.example.banking.interbank.application.out.OrderChangeNotifier;
import com.example.banking.interbank.application.out.OrderEngine;
import com.example.banking.interbank.application.out.OrderEventPublisher;
import com.example.banking.interbank.application.out.OrderStatusStore;
import com.example.banking.interbank.application.out.TransferExecutor;
import com.example.banking.interbank.application.out.TransferOrderQuery;
import com.example.banking.interbank.domain.retry.RetryPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = InterbankTransferApplication.class)
@Import(TestChannelBinderConfiguration.class)
@ActiveProfiles("test")
class ApplicationStartupTest {

    @Autowired
    ApplicationContext context;
    @Autowired
    Environment environment;

    @Test
    void should_wire_the_use_case_and_every_outbound_port_to_its_adapter() {
        assertThat(context.getBean(HandleTransferOrderEvent.class)).isNotNull();
        assertThat(context.getBean(OrderStatusStore.class)).isNotNull();
        assertThat(context.getBean(TransferOrderQuery.class)).isNotNull();
        assertThat(context.getBean(TransferExecutor.class)).isNotNull();
        assertThat(context.getBean(OrderEngine.class)).isNotNull();
        assertThat(context.getBean(OrderEventPublisher.class)).isInstanceOf(StreamBridgeOrderEventPublisher.class);
        assertThat(context.getBean(OrderChangeNotifier.class)).isInstanceOf(RedisOrderChangeNotifier.class);
    }

    @Test
    void should_expose_the_receive_function_to_spring_cloud_stream() {
        Object receive = context.getBean(FunctionCatalog.class).lookup("receive");
        assertThat(receive).isNotNull();
    }

    @Test
    void should_build_the_retry_policy_from_the_environment_variables() {
        assertThat(context.getBean(RetryPolicy.class)).isEqualTo(new RetryPolicy(0, 30, 120));
    }

    @Test
    void should_publish_retries_to_the_binding_that_has_a_destination_configured() {
        String binding = environment.getRequiredProperty("interbank.retry.binding");

        assertThat(binding).isEqualTo("retry-out-0");
        assertThat(environment.getProperty("spring.cloud.stream.bindings." + binding + ".destination"))
                .isEqualTo("interbank-topic");
    }
}
