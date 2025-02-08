package dev.andrew.lake.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.andrew.lake.config.OpenAiConfiguration;
import io.github.sashirestela.openai.domain.chat.ChatMessage.SystemMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import reactor.core.publisher.Flux;

@RestController
public class ChatController extends AiController {

    public ChatController(OpenAiConfiguration openAiConfiguration) {
        super(openAiConfiguration);
    }

    @PostMapping("/v1/chat")
    public String chat(@RequestBody String prompt) {
        ChatRequest chat = baseBuilder()
                .message(SystemMessage.of("You are an expert in AI."))
                .message(UserMessage.of(prompt))
                .build();

        return super.chat(chat);
    }

    @PostMapping(value = "/v1/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody String prompt) {
        var chat = baseBuilder()
                .message(SystemMessage.of("You are an expert in AI."))
                .message(UserMessage.of(prompt))
                .build();

        return Flux.fromStream(super.chatStream(chat));
    }

}
