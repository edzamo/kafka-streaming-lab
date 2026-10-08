package com.example.banking.interbank.adapter.out.redis;

import com.example.banking.interbank.application.out.TransferStepFailedException;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisOrderChangeNotifierTest {

    private final ReactiveStringRedisTemplate redis = mock(ReactiveStringRedisTemplate.class);
    private final RedisOrderChangeNotifier notifier = new RedisOrderChangeNotifier(redis, "INTERBANK");

    @Test
    void should_publish_the_order_id_on_the_configured_channel() {
        when(redis.convertAndSend("INTERBANK", "{\"trayOrderId\":114}")).thenReturn(Mono.just(1L));

        StepVerifier.create(notifier.notifyChanged(114L)).verifyComplete();

        verify(redis).convertAndSend("INTERBANK", "{\"trayOrderId\":114}");
    }

    @Test
    void should_not_publish_until_subscribed() {
        notifier.notifyChanged(114L);

        org.mockito.Mockito.verifyNoInteractions(redis);
    }

    @Test
    void should_translate_a_redis_failure_into_a_step_failure() {
        when(redis.convertAndSend("INTERBANK", "{\"trayOrderId\":114}"))
                .thenReturn(Mono.error(new IllegalStateException("redis down")));

        StepVerifier.create(notifier.notifyChanged(114L))
                .expectErrorSatisfies(error -> assertThat(error).isInstanceOf(TransferStepFailedException.class)
                        .extracting(e -> ((TransferStepFailedException) e).step()).isEqualTo("notify"))
                .verify();
    }
}
