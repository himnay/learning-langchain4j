package com.org.llm.backend;

import reactor.core.publisher.Flux;

/**
 * Strategy for where chat completions are executed: through {@code llm-gateway} or directly
 * against the local Spring AI {@code ChatClient}. Exactly one implementation is active per run,
 * selected at startup by {@code app.gateway.enabled} (see {@link GatewayChatBackend} /
 * {@link LocalChatBackend}).
 */
public interface ChatBackend {

    String chat(String systemPrompt, String conversationId, String message);

    Flux<String> stream(String conversationId, String message);
}
