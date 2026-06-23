package com.org.llm.backend;

import com.org.llm.client.GatewayClient;
import com.org.llm.model.ChatAnswer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Routes chat through {@code llm-gateway} (it owns provider keys, guardrails, failover and
 * per-session memory keyed by the conversation id). The gateway has no RAG integration, so this
 * backend never produces citations or a faithfulness verdict.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GatewayChatBackend implements ChatBackend {

    private final GatewayClient gatewayClient;

    @Override
    public ChatAnswer chat(String systemPrompt, String conversationId, String message, String documentSource) {
        log.info("CHAT | routing via gateway | session={}", conversationId);
        String answer = gatewayClient.chat(systemPrompt, message, conversationId);
        // ChatAnswer used to expose a withoutRag(String) static factory for this (no RAG
        // integration here, so no citations/faithfulness verdict); openapi-generator-generated
        // POJOs only carry getters/setters/constructors, not arbitrary static methods, so this is
        // inlined at its one call site instead.
        return new ChatAnswer(answer, List.of(), null);
    }

    @Override
    public Flux<ServerSentEvent<String>> stream(String conversationId, String message, String documentSource) {
        log.info("CHAT | streaming via gateway | session={}", conversationId);
        Flux<ServerSentEvent<String>> tokens = gatewayClient.streamChat(message, conversationId)
                .map(token -> ServerSentEvent.<String>builder().event("token").data(token).build());
        return tokens.concatWith(Flux.just(
                ServerSentEvent.<String>builder().event("citations").data("[]").build()));
    }
}
