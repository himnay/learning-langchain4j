package com.org.llm.backend;

/**
 * Strategy for image generation: {@code llm-gateway} (OpenAI DALL·E) or local Stability AI.
 * Selected at startup by {@code app.gateway.enabled}.
 */
public interface ImageBackend {

    byte[] generatePng(String message, String style, Integer count);
}
