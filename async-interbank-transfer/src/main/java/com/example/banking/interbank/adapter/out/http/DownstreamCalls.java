package com.example.banking.interbank.adapter.out.http;

import com.example.banking.interbank.application.in.RequestContext;
import com.example.banking.interbank.application.out.TransferStepFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

/** What every outbound HTTP client shares: request headers and translation of failures into step failures. */
public final class DownstreamCalls {

    private static final Logger log = LoggerFactory.getLogger(DownstreamCalls.class);

    private final HeaderDefaults defaults;
    private final Clock clock;
    private final Duration timeout;

    public DownstreamCalls(HeaderDefaults defaults, Clock clock, Duration timeout) {
        this.defaults = defaults;
        this.clock = clock;
        this.timeout = timeout;
    }

    RequestHeaders headers(RequestContext ctx) {
        return RequestHeaders.from(ctx, defaults, clock);
    }

    HeaderDefaults defaults() {
        return defaults;
    }

    /** Runs the call lazily; any failure or timeout becomes a {@link TransferStepFailedException}. */
    <T> Mono<T> guard(String step, java.util.function.Supplier<Mono<T>> call) {
        return Mono.defer(call).timeout(timeout).onErrorMap(failure -> {
            log.warn("Downstream step {} failed: {}", step, describe(failure));
            return new TransferStepFailedException(step, failure);
        });
    }

    private static String describe(Throwable failure) {
        if (failure instanceof WebClientResponseException http) {
            return "HTTP " + http.getStatusCode().value();
        }
        if (failure instanceof TimeoutException) {
            return "timeout";
        }
        return failure.getClass().getSimpleName();
    }
}
