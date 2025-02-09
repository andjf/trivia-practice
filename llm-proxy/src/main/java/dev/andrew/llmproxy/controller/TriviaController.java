package dev.andrew.llmproxy.controller;

import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.andrew.llmproxy.config.OpenAiConfiguration;
import dev.andrew.llmproxy.model.TriviaQuestion;
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
                questions for other players to answer. Try not
                to repeat questions by writing unique questions.
                Ensure your questions have distinct answers (the
                answers are not too similar to each other) and
                ensure that the questions are not opinion-based.
                """;

        String userMessage = String.format("""
                Player:
                I want you to generate a trivia question about the topic [%s]
                with a level of difficulty that can be described as [%s]
                """,
                topic,
                difficulty);

        ChatRequest chat = super.baseBuilder()
                .temperature(1.0)
                .message(SystemMessage.of(normalizeMessage(systemMessage)))
                .message(UserMessage.of(normalizeMessage(userMessage)))
                .responseFormat(ResponseFormat.jsonSchema(TriviaQuestion.jsonSchema()))
                .build();

        return super.chat(chat);
    }

}
