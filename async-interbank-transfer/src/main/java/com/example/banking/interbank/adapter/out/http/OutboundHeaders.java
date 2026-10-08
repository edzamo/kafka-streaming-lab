package com.example.banking.interbank.adapter.out.http;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Adds headers the generated clients do not declare (idempotency key, gateway headers...). The call site
 * puts them in the Reactor context with {@link #attach}; the filter copies them onto the outgoing request.
 */
public final class OutboundHeaders {

    private static final String CONTEXT_KEY = "interbank.outbound-headers";

    private OutboundHeaders() {
    }

    public static ExchangeFilterFunction filter() {
        return (request, next) -> Mono.deferContextual(context -> {
            Map<String, String> extra = context.getOrDefault(CONTEXT_KEY, Map.of());
            ClientRequest enriched = ClientRequest.from(request)
                    .headers(headers -> extra.forEach((name, value) -> {
                        if (value != null) {
                            headers.set(name, value);
                        }
                    })).build();
            return next.exchange(enriched);
        });
    }

    static <T> Mono<T> attach(Mono<T> call, Map<String, String> headers) {
        return call.contextWrite(context -> context.put(CONTEXT_KEY, headers));
    }
}
