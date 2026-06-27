package com.org.llm.backend;

import com.org.llm.model.ChatAnswer;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * Strategy for where chat completions are executed: through {@code llm-gateway} or directly
 * against the local LangChain4j {@code ChatAssistant}. Exactly one implementation is active per run,
 * selected at startup by {@code app.gateway.enabled} (see {@link GatewayChatBackend} /
 * {@link LocalChatBackend}).
 */
public interface ChatBackend {

    default ChatAnswer chat(String systemPrompt, String conversationId, String message) {
        return chat(systemPrompt, conversationId, message, null);
    }

    /**
     * @param documentSource when non-null, scopes RAG retrieval to documents whose {@code fileName}
     *                       metadata matches this value (see {@code RagFilterContext}); ignored by
     *                       backends without RAG integration (e.g. {@code GatewayChatBackend}).
     */
    ChatAnswer chat(String systemPrompt, String conversationId, String message, String documentSource);

    default Flux<ServerSentEvent<String>> stream(String systemPrompt, String conversationId, String message) {
        return stream(systemPrompt, conversationId, message, null);
    }

    /**
     * Streams the answer as SSE: zero or more {@code event: token} events carrying raw answer
     * text chunks, followed by exactly one trailing {@code event: citations} event carrying the
     * RAG citations (as JSON; an empty array when the backend doesn't do RAG, e.g. the gateway
     * path).
     *
     * @param documentSource when non-null, scopes RAG retrieval to documents whose {@code fileName}
     *                       metadata matches this value; ignored by backends without RAG integration.
     */
    Flux<ServerSentEvent<String>> stream(String systemPrompt, String conversationId, String message, String documentSource);
}
