package com.org.llm.backend;

import com.org.llm.exception.UpstreamServiceException;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

/**
 * Generates images with OpenAI Dall-E via LangChain4j's {@link ImageModel}. LangChain4j has no
 * Stability AI integration at all (verified — not even in its community modules), so this no
 * longer talks to Stable Diffusion; the "style preset" Stability had is folded into the prompt
 * text instead, same as {@code GatewayImageBackend} already does for its DALL·E path.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.gateway", name = "enabled", havingValue = "false")
public class LocalImageBackend implements ImageBackend {

    private final ImageModel imageModel;

    public LocalImageBackend(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    @Override
    public byte[] generatePng(String message, String style, Integer count) {
        String prompt = (style == null || style.isBlank()) ? message : message + ", " + style + " style";
        Response<List<Image>> response = imageModel.generate(prompt, count);
        Image image = response.content().get(0);
        if (image.base64Data() == null) {
            throw new UpstreamServiceException("OpenAI returned an image URL instead of base64 data: " + image.url());
        }
        return Base64.getDecoder().decode(image.base64Data());
    }
}
