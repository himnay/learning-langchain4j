package com.org.llm.service;

import com.org.llm.backend.ChatBackend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Application layer for chat: normalizes the conversation id, builds the system prompt and
 * delegates execution to the active {@link ChatBackend} strategy (gateway or local).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatBackend chatBackend;

    public String chat(String conversationId, String message) {
        return chatBackend.chat(systemPrompt(), normalizeConversationId(conversationId), message);
    }

    public Flux<String> streamChat(String conversationId, String message) {
        return chatBackend.stream(normalizeConversationId(conversationId), message);
    }

    private static String systemPrompt() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        return "Today’s date is " + today + ". " +
                "You are a friendly travel guide. Suggest 3 attractions and 1 food item.";
    }

    private static String normalizeConversationId(String conversationId) {
        return (conversationId == null || conversationId.isBlank())
                ? UUID.randomUUID().toString()
                : conversationId;
    }
}
