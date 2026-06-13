package com.org.llm.controller;

import com.org.llm.backend.ImageBackend;
import com.org.llm.model.ChatRequest;
import com.org.llm.service.ImageCaptionService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/image")
class ImageRestController {

    private final ImageCaptionService imageCaptionService;
    private final ImageBackend imageBackend;

    @PostMapping("/caption")
    public String caption(@Validated @RequestBody ChatRequest chatRequest) {
        return imageCaptionService.captionImage(chatRequest.imageName(), chatRequest.message());
    }

    @GetMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateImage(
            @NotBlank(message = "message is required") @RequestParam String message,
            @NotBlank(message = "style is required") @RequestParam String style,
            @Positive(message = "count must be a positive number") @RequestParam Integer count) {
        byte[] png = imageBackend.generatePng(message, style, count);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }
}
