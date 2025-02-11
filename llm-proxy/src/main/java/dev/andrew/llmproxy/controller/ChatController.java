package dev.andrew.llmproxy.controller;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.andrew.llmproxy.config.OpenAiConfiguration;
import io.github.sashirestela.openai.domain.chat.ChatMessage.SystemMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
public class ChatController extends AiController {

    public ChatController(OpenAiConfiguration openAiConfiguration) {
        super(openAiConfiguration);
    }

    @PostMapping("/v1/chat")
    public String chat(@RequestBody String prompt) {
        log.info("Initiated /v1/chat request with prmopt [{}]", prompt);

        ChatRequest chat = baseBuilder()
                .message(SystemMessage.of("You are an expert in AI."))
                .message(UserMessage.of(prompt))
                .build();

        return super.chat(chat);
    }

    @PostMapping(value = "/v1/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody String prompt) {
        log.info("Initiated /v1/chat/stream request with prmopt [{}]", prompt);

        ChatRequest chat = baseBuilder()
                .message(SystemMessage.of("You are an expert in AI."))
                .message(UserMessage.of(prompt))
                .build();

        return Flux.fromStream(super.chatStream(chat)
                .map(token -> new JSONObject(Map.of("token", token)))
                .map(JSONObject::toString));
    }

}
