package com.example.banking.interbank.adapter.out.http;

import com.example.banking.interbank.application.in.RequestContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

/** One MockWebServer plus the pieces every outbound HTTP client test needs. */
class HttpFixture implements AutoCloseable {

    static final RequestContext CTX = new RequestContext("req-1", "INTERBANK04", "Banking", "session-secret",
            "ES", "aff-secret", "user-secret", "web", "APPROVE");

    private static final ObjectMapper JSON = new ObjectMapper();

    final MockWebServer server = new MockWebServer();
    final HeaderDefaults defaults =
            new HeaderDefaults("async-interbank-transfer", "DefaultApp", "dev-1", "10.0.0.1", "agency-1", "geo-1");
    final Clock clock = Clock.fixed(Instant.parse("2026-03-04T05:06:07Z"), ZoneOffset.UTC);
    final DownstreamCalls calls = new DownstreamCalls(defaults, clock, Duration.ofSeconds(2));

    HttpFixture() {
        try {
            server.start();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    WebClient webClient() {
        return DownstreamWebClient.create();
    }

    String baseUrl() {
        return server.url("/api/v1").toString();
    }

    void respond(int status, String body) {
        server.enqueue(new MockResponse().setResponseCode(status)
                .setHeader("Content-Type", "application/json").setBody(body));
    }

    RecordedRequest takeRequest() throws InterruptedException {
        return server.takeRequest(2, java.util.concurrent.TimeUnit.SECONDS);
    }

    static JsonNode json(RecordedRequest request) throws IOException {
        return JSON.readTree(request.getBody().readUtf8());
    }

    @Override
    public void close() throws IOException {
        server.shutdown();
    }
}
