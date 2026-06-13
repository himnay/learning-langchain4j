package com.org.llm.backend;

import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Base64;

/** Generates images directly with Stability AI (stable-diffusion). */
@Component
@ConditionalOnProperty(prefix = "app.gateway", name = "enabled", havingValue = "false")
public class LocalImageBackend implements ImageBackend {

    private final ImageModel imageModel;

    public LocalImageBackend(@Qualifier("stabilityAiImageModel") ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    @Override
    public byte[] generatePng(String message, String style, Integer count) {
        ImagePrompt prompt = new ImagePrompt(message,
                StabilityAiImageOptions.builder()
                        .stylePreset(style)
                        .n(count)
                        .responseFormat("b64_json")
                        .build());

        ImageResponse imageResponse = imageModel.call(prompt);
        return Base64.getDecoder().decode(imageResponse.getResult().getOutput().getB64Json());
    }
}
