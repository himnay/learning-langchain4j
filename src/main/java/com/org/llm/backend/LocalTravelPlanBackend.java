package com.org.llm.backend;

import com.org.llm.model.TravelPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Uses the local {@code ChatClient}'s structured-output support to map straight to {@link TravelPlan}. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.gateway", name = "enabled", havingValue = "false")
public class LocalTravelPlanBackend implements TravelPlanBackend {

    private final ChatClient chatClient;

    @Override
    public TravelPlan plan(PromptTemplate template, Map<String, Object> variables) {
        // .entity() automatically appends JSON format instructions; no extra UserMessage needed
        return chatClient.prompt(template.create(variables))
                .call()
                .entity(TravelPlan.class);
    }
}
