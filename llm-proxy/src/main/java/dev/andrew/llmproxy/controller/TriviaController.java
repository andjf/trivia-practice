package dev.andrew.llmproxy.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.andrew.llmproxy.config.OpenAiConfiguration;
import dev.andrew.llmproxy.function.LifeFunction;
import dev.andrew.llmproxy.model.TriviaQuestion;
import io.github.sashirestela.openai.common.ResponseFormat;
import io.github.sashirestela.openai.common.function.FunctionCall;
import io.github.sashirestela.openai.common.function.FunctionDef;
import io.github.sashirestela.openai.common.function.FunctionExecutor;
import io.github.sashirestela.openai.common.tool.ToolCall;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.ResponseMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.SystemMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.ToolMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage;
import lombok.extern.slf4j.Slf4j;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import io.github.sashirestela.openai.domain.chat.Chat.Choice;

@Slf4j
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

    // @GetMapping("/")
    // public String test(@RequestParam String prompt) {
    //     var functionExecutor = new FunctionExecutor();
    //     functionExecutor.enrollFunction(FunctionDef.builder()
    //             .name("life_death_function")
    //             .description("Get the birth and possible death date of any individual mentioned")
    //             .functionalClass(LifeFunction.class)
    //             .strict(Boolean.TRUE)
    //             .build());
    //     var messages = new ArrayList<ChatMessage>();
    //     messages.add(UserMessage.of(prompt));
    //     var chatRequest = ChatRequest.builder()
    //             .model("gpt-4o-mini")
    //             .messages(messages)
    //             .tools(functionExecutor.getToolFunctions())
    //             .build();
    //     CompletableFuture<Chat> futureChat = openAI.chatCompletions().create(chatRequest);
    //     Chat chatResponse = futureChat.join();
    //     ResponseMessage chatMessage = chatResponse.firstMessage();
    //     ToolCall chatToolCall = chatMessage.getToolCalls().get(0);
    //     Object result = functionExecutor.execute(chatToolCall.getFunction());
    //     messages.add(chatMessage);
    //     messages.add(ToolMessage.of(result.toString(), chatToolCall.getId()));
    //     chatRequest = ChatRequest.builder()
    //             .model("gpt-4o-mini")
    //             .messages(messages)
    //             .tools(functionExecutor.getToolFunctions())
    //             .build();
    //     futureChat = openAI.chatCompletions().create(chatRequest);
    //     chatResponse = futureChat.join();
    //     return chatResponse.firstContent();
    // }

    // @GetMapping("/stream")
    // public String stream(@RequestParam String prompt) {
    //     var functionExecutor = new FunctionExecutor();
    //     functionExecutor.enrollFunction(FunctionDef.builder()
    //             .name("life_death_function")
    //             .description("Get the birth and possible death date of any individual mentioned")
    //             .functionalClass(LifeFunction.class)
    //             .strict(Boolean.TRUE)
    //             .build());

    //     var messages = new ArrayList<ChatMessage>();
    //     messages.add(UserMessage.of(prompt));

    //     var chatRequest = ChatRequest.builder()
    //             .model("gpt-4o-mini")
    //             .messages(messages)
    //             .tools(functionExecutor.getToolFunctions())
    //             .build();

    //     CompletableFuture<Stream<Chat>> futureChat = openAI.chatCompletions().createStream(chatRequest);
    //     Stream<Chat> chatResponse = futureChat.join();
    //     ObjectMapper mapper = new ObjectMapper();
    //     List<FunctionCall> functionCalls = new ArrayList<FunctionCall>();
    //     chatResponse.forEach(chat -> {
    //         if (chat.getChoices() != null && chat.getChoices().isEmpty()) {
    //             log.info("Ignoring message {} because it is missing choices", chat);
    //             return;
    //         }
    //         ResponseMessage firstMessage = Optional.ofNullable(chat.getChoices().get(0)).map(Choice::getMessage).orElse(null);
    //         if (firstMessage != null && firstMessage.getToolCalls() != null && !firstMessage.getToolCalls().isEmpty()) {
    //             ToolCall partialFunctionCall = firstMessage.getToolCalls().get(0);
    //             if (partialFunctionCall.getName() != null) {

    //             }
    //             FunctionCall currentFunctionCall = new FunctionCall();
    //         }
    //         try {
    //             chat.getChoices();
    //             log.info(mapper.writeValueAsString(chat));
    //         } catch (JsonProcessingException e) {
    //             log.error("Error encountered", e);
    //         }
    //     });
    //     return "Done.";
    // }

}
