package com.org.llm.backend;

import com.org.llm.client.GatewayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Generates images via {@code llm-gateway} (OpenAI DALL·E). The Stability "style preset" has no
 * DALL·E equivalent, so it's folded into the prompt instead.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GatewayImageBackend implements ImageBackend {

    private final GatewayClient gatewayClient;

    @Override
    public byte[] generatePng(String message, String style, Integer count) {
        log.info("IMAGE | routing via gateway | style={} count={}", style, count);
        String prompt = (style == null || style.isBlank()) ? message : message + ", " + style + " style";
        return gatewayClient.generateImage(prompt, count);
    }
}
