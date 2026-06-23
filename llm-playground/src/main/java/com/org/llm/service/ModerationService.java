package com.org.llm.service;

import com.org.llm.model.ModerationCheckResult;
import dev.langchain4j.model.moderation.Moderation;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Direct {@code ModerationModel} usage, distinct from the {@code @Moderate} annotation used in
 * {@code llm-chat-agent} (which moderates input automatically inside an {@code AiServices} call).
 * This calls the model standalone, for content that isn't part of any chat flow.
 */
@Service
@RequiredArgsConstructor
public class ModerationService {

    private final ModerationModel moderationModel;

    public ModerationCheckResult check(String text) {
        Response<Moderation> response = moderationModel.moderate(text);
        Moderation moderation = response.content();
        return ModerationCheckResult.builder()
                .flagged(moderation.flagged())
                .flaggedText(moderation.flaggedText())
                .build();
    }
}
