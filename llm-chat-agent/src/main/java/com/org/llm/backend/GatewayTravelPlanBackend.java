package com.org.llm.backend;

import com.org.llm.client.GatewayClient;
import com.org.llm.exception.ChatProviderException;
import com.org.llm.model.TravelPlan;
import dev.langchain4j.model.input.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Renders the prompt locally, asks the gateway for strict JSON and deserializes it here
 * (the gateway returns free-form text, not a typed entity).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GatewayTravelPlanBackend implements TravelPlanBackend {

    /**
     * JSON shape requested from the gateway so the free-form completion can be deserialized.
     */
    private static final String JSON_INSTRUCTION = """
            Respond with ONLY valid JSON (no markdown, no prose) matching exactly:
            {"city": string, "days": number, "itinerary": [{"day": number, "activities": string, "food": string, "budget": number}]}""";

    private final GatewayClient gatewayClient;
    private final ObjectMapper objectMapper;

    /**
     * Defensive: strip ```json fences if a model wraps the JSON despite instructions.
     */
    private static String stripFences(String text) {
        String cleaned = text == null ? "" : text.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("(?s)^```(?:json)?", "").replaceFirst("(?s)```$", "").trim();
        }
        return cleaned;
    }

    @Override
    public TravelPlan plan(PromptTemplate template, Map<String, Object> variables) {
        log.info("TRAVEL | routing via gateway | vars={}", variables);
        String rendered = template.apply(variables).text();
        String json = gatewayClient.query(JSON_INSTRUCTION, rendered);
        return parse(json);
    }

    private TravelPlan parse(String json) {
        try {
            return objectMapper.readValue(stripFences(json), TravelPlan.class);
        } catch (Exception ex) {
            throw new ChatProviderException("Gateway returned a travel plan that could not be parsed: "
                    + ex.getMessage());
        }
    }
}
