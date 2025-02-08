package dev.andrew.lake.controller;

import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.andrew.lake.config.OpenAiConfiguration;
import dev.andrew.lake.model.TriviaQuestion;
import io.github.sashirestela.openai.common.ResponseFormat;
import io.github.sashirestela.openai.domain.chat.ChatMessage.SystemMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;

@RestController
@RequestMapping("/trivia")
public class TriviaController extends AiController {

    public TriviaController(OpenAiConfiguration openAiConfiguration) {
        super(openAiConfiguration);
    }

    private String normalizeMessage(String message) {
        return message.lines().collect(Collectors.joining(" "));
    }

    @GetMapping(value = "/new", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getMethodName(@RequestParam String topic, @RequestParam String difficulty) {
        String systemMessage = """
                You are the the host of a triva game night and
                you've been tasked with coming up with trivia
                questions for other players to answer.
                """;

        String userMessage = String.format("""
                I want you to generate a trivia question of [%s]
                difficulty on the topic of [%s]
                """,
                difficulty,
                topic);

        ChatRequest chat = super.baseBuilder()
                .message(SystemMessage.of(normalizeMessage(systemMessage)))
                .message(UserMessage.of(normalizeMessage(userMessage)))
                .responseFormat(ResponseFormat.jsonSchema(TriviaQuestion.jsonSchema()))
                .build();

        return super.chat(chat);
    }

}
