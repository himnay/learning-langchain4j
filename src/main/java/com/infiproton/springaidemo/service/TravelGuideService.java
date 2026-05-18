package com.infiproton.springaidemo.service;

import com.infiproton.springaidemo.model.TravelPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TravelGuideService {

    private final ChatClient chatClient;

    @Value("classpath:prompts/travel-guide.st")
    private Resource travelGuideTemplate;

    public TravelPlan prepareTravelPlan(String city, Integer days) {
        PromptTemplate template = new PromptTemplate(travelGuideTemplate);

        Map<String, Object> params = Map.of(
                "city", city,
                "days", days
        );
        Prompt prompt = new Prompt(List.of(template.createMessage(params), new UserMessage("Please provide a travel plan in JSON format.")));

        return chatClient.prompt(prompt)
                .call()
                .entity(TravelPlan.class);
    }

}
