package com.infiproton.springaidemo.controller;

import com.infiproton.springaidemo.model.ChatRequest;
import com.infiproton.springaidemo.service.ImageCaptionService;
import com.infiproton.springaidemo.service.ImageGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/image")
class ImageRestController {

    private final ImageCaptionService imageCaptionService;
    private final ImageGenerationService imageGenerationService;

    @PostMapping("/caption")
    public String caption(@RequestBody ChatRequest chatRequest) {
        return imageCaptionService.captionImage(chatRequest.imageName(), chatRequest.message());
    }

    @GetMapping("/generate")
    public List<String> generateImage(@RequestParam String message, @RequestParam String style, @RequestParam Integer count) {
        return imageGenerationService.generate(message, style, count);
    }
}
