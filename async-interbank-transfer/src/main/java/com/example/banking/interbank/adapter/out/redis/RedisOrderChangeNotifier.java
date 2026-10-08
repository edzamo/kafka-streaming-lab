package com.example.banking.interbank.adapter.out.redis;

import com.example.banking.interbank.application.out.OrderChangeNotifier;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

public class RedisOrderChangeNotifier implements OrderChangeNotifier {

    private static final Logger log = LoggerFactory.getLogger(RedisOrderChangeNotifier.class);

    private final ReactiveStringRedisTemplate redis;
    private final String channel;

    public RedisOrderChangeNotifier(ReactiveStringRedisTemplate redis, String channel) {
        this.redis = redis;
        this.channel = channel;
    }

    @Override
    public Mono<Void> notifyChanged(long orderId) {
        return Mono.defer(() -> redis.convertAndSend(channel, "{\"trayOrderId\":" + orderId + "}"))
                .onErrorMap(failure -> {
                    log.warn("Notifying change of order {} failed: {}", orderId, failure.getClass().getSimpleName());
                    return new TransferStepFailedException("notify", failure);
                })
                .then();
    }
}
