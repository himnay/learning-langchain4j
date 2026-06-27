package com.org.llm.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Moderate;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * LangChain4j {@code AiServices} contract for the main conversational agent — built in
 * {@code AIConfig} with the guardrail, chat-memory provider, RAG retrieval augmentor, tools and
 * moderation model all wired onto a single implementation, the same role Spring AI's
 * {@code ChatClient} + advisor chain played.
 */
public interface ChatAssistant {

    @Moderate
    @SystemMessage("{{systemPrompt}}")
    String chat(@MemoryId String conversationId, @V("systemPrompt") String systemPrompt, @UserMessage String message);

    @Moderate
    @SystemMessage("{{systemPrompt}}")
    TokenStream chatStream(@MemoryId String conversationId, @V("systemPrompt") String systemPrompt, @UserMessage String message);
}
