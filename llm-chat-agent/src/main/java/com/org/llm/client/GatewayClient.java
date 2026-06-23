package com.org.llm.client;

import com.org.llm.client.dto.GatewayChatRequest;
import com.org.llm.client.dto.GatewayChatResponse;
import com.org.llm.config.GatewayProperties;
import com.org.llm.exception.ChatProviderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Thin client over {@code llm-gateway}'s HTTP API ({@code /llm/chat}, {@code /llm/query},
 * {@code /{provider}/stream}). Blocking calls use {@code .block()} with the configured timeout;
 * streaming returns the gateway's SSE token stream as a {@link Flux}.
 */
@Slf4j
@Component
public class GatewayClient {

    private final WebClient webClient;
    private final GatewayProperties properties;

    public GatewayClient(WebClient gatewayWebClient, GatewayProperties properties) {
        this.webClient = gatewayWebClient;
        this.properties = properties;
    }

    /**
     * Multi-turn chat via {@code POST /llm/chat} (session-backed memory lives in the gateway).
     */
    public String chat(String systemPrompt, String userPrompt, String sessionId) {
        GatewayChatResponse response = webClient.post()
                .uri("/chat")
                .bodyValue(new GatewayChatRequest(userPrompt, properties.getProvider(),
                        modelOrNull(), systemPrompt, sessionId))
                .retrieve()
                .bodyToMono(GatewayChatResponse.class)
                .block(timeout());
        return extractText(response);
    }

    /**
     * One-shot, stateless completion via {@code POST /llm/query} (used for structured extraction).
     */
    public String query(String systemPrompt, String userPrompt) {
        GatewayChatResponse response = webClient.post()
                .uri("/query")
                .bodyValue(new GatewayChatRequest(userPrompt, properties.getProvider(),
                        modelOrNull(), systemPrompt, null))
                .retrieve()
                .bodyToMono(GatewayChatResponse.class)
                .block(timeout());
        return extractText(response);
    }

    /**
     * Streaming chat via {@code POST /llm/{provider}/stream} (Server-Sent Events of token chunks).
     */
    public Flux<String> streamChat(String userPrompt, String sessionId) {
        return webClient.post()
                .uri("/{provider}/stream", properties.getProvider())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(new GatewayChatRequest(userPrompt, properties.getProvider(),
                        modelOrNull(), null, sessionId))
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorResume(ex -> {
                    log.error("GATEWAY | stream failed | {}", ex.getMessage());
                    return Flux.error(new ChatProviderException("Gateway stream failed: " + ex.getMessage()));
                });
    }

    private String extractText(GatewayChatResponse response) {
        if (response == null) {
            throw new ChatProviderException("Gateway returned no response");
        }
        if (response.error() != null) {
            throw new ChatProviderException("Gateway error: " + response.error());
        }
        return response.text();
    }

    private String modelOrNull() {
        return properties.getModel() == null || properties.getModel().isBlank() ? null : properties.getModel();
    }

    private Duration timeout() {
        return Duration.ofSeconds(properties.getTimeoutSeconds());
    }
}
