package com.example.banking.interbank.config;

import com.example.banking.interbank.adapter.out.http.DownstreamCalls;
import com.example.banking.interbank.adapter.out.http.DownstreamClients;
import com.example.banking.interbank.adapter.out.http.HeaderDefaults;
import com.example.banking.interbank.adapter.out.messaging.StreamBridgeOrderEventPublisher;
import com.example.banking.interbank.adapter.out.redis.RedisOrderChangeNotifier;
import com.example.banking.interbank.application.out.OrderChangeNotifier;
import com.example.banking.interbank.application.out.OrderEngine;
import com.example.banking.interbank.application.out.OrderEventPublisher;
import com.example.banking.interbank.application.out.OrderStatusStore;
import com.example.banking.interbank.application.out.TransferExecutor;
import com.example.banking.interbank.application.out.TransferOrderQuery;
import com.example.banking.interbank.application.service.ProcessSettings;
import com.example.banking.interbank.domain.retry.RetryPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Clock;

/**
 * Composition root for everything that is not a plain component: configuration values reach each adapter
 * here, and only here. Use cases and adapters that need no scalar value are wired by component scan.
 */
@Configuration
@EnableConfigurationProperties({RetryProperties.class, ProcessProperties.class, NotificationProperties.class,
        DownstreamProperties.class})
class InterbankConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    Scheduler retryScheduler() {
        return Schedulers.parallel();
    }

    @Bean
    RetryPolicy retryPolicy(RetryProperties properties) {
        return new RetryPolicy(properties.initialSeconds(), properties.stepSeconds(), properties.limitSeconds());
    }

    @Bean
    ProcessSettings processSettings(ProcessProperties properties) {
        return new ProcessSettings(properties.executeCode(), properties.modifyCode(), properties.errorStatus());
    }

    @Bean
    DownstreamCalls downstreamCalls(DownstreamProperties properties, Clock clock) {
        return new DownstreamCalls(new HeaderDefaults(properties.callerName(), properties.appName(),
                properties.device(), properties.deviceIp(), properties.agency(), properties.geolocation()),
                clock, properties.timeout());
    }

    @Bean
    OrderStatusStore orderStatusStore(DownstreamProperties properties, DownstreamCalls calls) {
        return DownstreamClients.orderStatusStore(properties.urls().spInterbankTransferOrder(), calls);
    }

    @Bean
    TransferOrderQuery transferOrderQuery(DownstreamProperties properties, DownstreamCalls calls) {
        return DownstreamClients.transferOrderQuery(properties.urls().bsInterbankTransferOrder(), calls);
    }

    @Bean
    TransferExecutor transferExecutor(DownstreamProperties properties, DownstreamCalls calls) {
        return DownstreamClients.transferExecutor(properties.urls().bsInterbankTransferOrder(), calls);
    }

    @Bean
    OrderEngine orderEngine(DownstreamProperties properties, DownstreamCalls calls) {
        return DownstreamClients.orderEngine(properties.urls().bsOrder(), calls, properties.systemUserId());
    }

    @Bean
    OrderEventPublisher orderEventPublisher(StreamBridge streamBridge, RetryProperties properties) {
        return new StreamBridgeOrderEventPublisher(streamBridge, properties.binding());
    }

    @Bean
    OrderChangeNotifier orderChangeNotifier(ReactiveStringRedisTemplate redis, NotificationProperties properties) {
        return new RedisOrderChangeNotifier(redis, properties.channel());
    }
}
