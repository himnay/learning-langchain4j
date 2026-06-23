package com.org.llm.client;

import com.org.llm.client.dto.GatewayImageRequest;
import com.org.llm.exception.UpstreamServiceException;
import com.org.llm.client.dto.GatewayImageResponse;
import com.org.llm.config.GatewayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Base64;
import java.util.List;

/**
 * Thin client over {@code llm-gateway}'s image endpoint ({@code POST /llm/image}). Blocking calls
 * use {@code .block()} with the configured timeout.
 */
@Slf4j
@Component
public class GatewayClient {

    private final WebClient webClient;
    private final GatewayProperties properties;

    public GatewayClient(WebClient gatewayWebClient, GatewayProperties properties) {
        this.webClient = gatewayWebClient;
        this.properties = properties;
    }

    /**
     * Image generation via {@code POST /llm/image}; returns the first image decoded from base64.
     */
    public byte[] generateImage(String prompt, Integer count) {
        GatewayImageResponse response = webClient.post()
                .uri("/image")
                .bodyValue(new GatewayImageRequest(prompt, properties.getImageModel(),
                        "1024x1024", count == null || count < 1 ? 1 : count, "b64_json"))
                .retrieve()
                .bodyToMono(GatewayImageResponse.class)
                .block(timeout());

        if (response == null || response.error() != null) {
            throw new UpstreamServiceException("Gateway image generation failed: "
                    + (response == null ? "no response" : response.error()));
        }
        List<String> images = response.images();
        if (images == null || images.isEmpty()) {
            throw new UpstreamServiceException("Gateway returned no image");
        }
        return Base64.getDecoder().decode(images.get(0));
    }

    private Duration timeout() {
        return Duration.ofSeconds(properties.getTimeoutSeconds());
    }
}
