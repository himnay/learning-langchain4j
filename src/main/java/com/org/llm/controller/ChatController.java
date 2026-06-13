package com.org.llm.controller;

import com.org.llm.model.ChatRequest;
import com.org.llm.model.TravelPlan;
import com.org.llm.service.ChatService;
import com.org.llm.service.TravelGuideService;
import com.org.llm.service.VoiceChatService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
class ChatController {

    private final ChatService chatService;
    private final TravelGuideService travelGuideService;
    private final VoiceChatService voiceChatService;
    private final ChatMemory chatMemory;

    @PostMapping
    public String chat(@Validated @RequestBody ChatRequest chatRequest) {
        return chatService.chat(chatRequest.conversationId(), chatRequest.message());
    }

    @PostMapping("/audio/voice")
    public ResponseEntity<byte[]> voiceChat(@RequestParam("file") MultipartFile file) {
        VoiceChatService.VoiceExchange exchange = voiceChatService.exchange(file);
        byte[] audioResponse = voiceChatService.speak(exchange.aiResponse());

        return ResponseEntity.ok()
                .header("Content-Type", "audio/mpeg")
                .header("X-Transcript", exchange.transcript())
                .header("X-AI-Response", exchange.aiResponse())
                .body(audioResponse);
    }

    @PostMapping("/audio")
    public Map<String, Object> chatWithAudio(@RequestParam("file") MultipartFile file) {
        VoiceChatService.VoiceExchange exchange = voiceChatService.exchange(file);
        return Map.of("transcript", exchange.transcript(), "aiResponse", exchange.aiResponse());
    }

    @GetMapping("/travel-guide")
    public TravelPlan prepareTravelPlan(
            @NotBlank(message = "city is required") @RequestParam String city,
            @Positive(message = "days must be a positive number") @RequestParam Integer days) {
        return travelGuideService.prepareTravelPlan(city, days);
    }

    @GetMapping("/memory")
    public List<Message> fetchMemory(
            @NotBlank(message = "conversationId is required") @RequestParam String conversationId) {
        return chatMemory.get(conversationId);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@Validated @RequestBody ChatRequest chatRequest) {
        return chatService.streamChat(chatRequest.conversationId(), chatRequest.message());
    }
}
