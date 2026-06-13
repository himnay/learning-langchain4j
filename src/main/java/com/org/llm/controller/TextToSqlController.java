package com.org.llm.controller;

import com.org.llm.model.TextToSqlRequest;
import com.org.llm.model.TextToSqlResponse;
import com.org.llm.service.TextToSqlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Validation and {@code SqlValidationException} failures are translated to 400 JSON by
 * {@link com.org.llm.exception.GlobalExceptionHandler}.
 */
@RestController
@RequiredArgsConstructor
class TextToSqlController {

    private final TextToSqlService textToSqlService;

    @PostMapping("/text-to-sql")
    public TextToSqlResponse textToSql(@Valid @RequestBody TextToSqlRequest request) {
        return textToSqlService.process(request);
    }
}
