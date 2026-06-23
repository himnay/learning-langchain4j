package com.org.llm.controller;

import com.org.llm.assistant.ClassifierAssistant;
import com.org.llm.assistant.ExtractionAssistant;
import com.org.llm.assistant.SummarizerAssistant;
import com.org.llm.model.ExtractedPerson;
import com.org.llm.model.ModerationCheckResult;
import com.org.llm.model.Sentiment;
import com.org.llm.model.TextRequest;
import com.org.llm.service.ModerationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Showcases LangChain4j {@code AiServices} output styles the other modules don't otherwise
 * exercise: enum classification, structured extraction, plain summarization, and a standalone
 * moderation check.
 */
@Validated
@RestController
@RequestMapping("/api/v1/playground")
@RequiredArgsConstructor
class PlaygroundController {

    private final ClassifierAssistant classifierAssistant;
    private final ExtractionAssistant extractionAssistant;
    private final SummarizerAssistant summarizerAssistant;
    private final ModerationService moderationService;

    @PostMapping("/classify")
    public Sentiment classify(@Valid @RequestBody TextRequest request) {
        return classifierAssistant.classifySentiment(request.getText());
    }

    @PostMapping("/extract")
    public ExtractedPerson extract(@Valid @RequestBody TextRequest request) {
        return extractionAssistant.extract(request.getText());
    }

    @PostMapping("/summarize")
    public String summarize(@Valid @RequestBody TextRequest request,
                            @Positive @RequestParam(defaultValue = "3") int maxSentences) {
        return summarizerAssistant.summarize(request.getText(), maxSentences);
    }

    @PostMapping("/moderate")
    public ModerationCheckResult moderate(@Valid @RequestBody TextRequest request) {
        return moderationService.check(request.getText());
    }
}
