package com.org.llm.backend;

import com.org.llm.tool.ContactsTool;
import com.org.llm.tool.WeatherTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Calls OpenAI directly via the local Spring AI {@code ChatClient}, with JDBC-backed
 * conversation memory and the weather/contacts tools.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.gateway", name = "enabled", havingValue = "false")
public class LocalChatBackend implements ChatBackend {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final WeatherTools weatherTools;
    private final ContactsTool contactsTool;

    @Override
    public String chat(String systemPrompt, String conversationId, String message) {
        // conversationId param is picked up by the default MessageChatMemoryAdvisor in AIConfig
        return chatClient.prompt()
                .system(systemPrompt)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(weatherTools, contactsTool)
                .user(message)
                .call()
                .content();
    }

    @Override
    public Flux<String> stream(String conversationId, String message) {
        // QuestionAnswerAdvisor adds RAG context from vector store on top of the default memory advisor
        return chatClient.prompt()
                .advisors(spec -> spec
                        .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(weatherTools, contactsTool)
                .user(message)
                .stream()
                .content()
                .onErrorResume(throwable -> {
                    log.error("Error occurred in the stream", throwable);
                    return Flux.error(new IllegalStateException(
                            "Error occurred in the stream: %s".formatted(throwable.getMessage())));
                });
    }
}
