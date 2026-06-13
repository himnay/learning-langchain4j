package com.org.llm.backend;

import com.org.llm.client.GatewayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Routes chat through {@code llm-gateway} (it owns provider keys, guardrails, failover and
 * per-session memory keyed by the conversation id).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GatewayChatBackend implements ChatBackend {

    private final GatewayClient gatewayClient;

    @Override
    public String chat(String systemPrompt, String conversationId, String message) {
        log.info("CHAT | routing via gateway | session={}", conversationId);
        return gatewayClient.chat(systemPrompt, message, conversationId);
    }

    @Override
    public Flux<String> stream(String conversationId, String message) {
        log.info("CHAT | streaming via gateway | session={}", conversationId);
        return gatewayClient.streamChat(message, conversationId);
    }
}
