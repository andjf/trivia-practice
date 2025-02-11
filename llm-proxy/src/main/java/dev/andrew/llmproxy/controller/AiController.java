package dev.andrew.llmproxy.controller;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import dev.andrew.llmproxy.config.OpenAiConfiguration;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.Chat.Choice;
import io.github.sashirestela.openai.domain.chat.ChatMessage.ResponseMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import io.github.sashirestela.openai.domain.chat.ChatRequest.ChatRequestBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AiController {

    private static final double DEFAULT_TEMP = 0.5;
    private static final int DEFAULT_MAX_TOKENS = 300;

    protected final OpenAiConfiguration openAiConfiguration;
    protected final SimpleOpenAI openAI;

    public AiController(OpenAiConfiguration openAiConfiguration) {
        this.openAiConfiguration = openAiConfiguration;
        this.openAI = SimpleOpenAI.builder()
                .apiKey(this.openAiConfiguration.apiKey())
                .build();
    }

    protected String model() {
        return this.openAiConfiguration.model();
    }

    protected ChatRequestBuilder baseBuilder() {
        return this.baseBuilder(DEFAULT_TEMP, DEFAULT_MAX_TOKENS);
    }

    protected ChatRequestBuilder baseBuilder(int maxTokens) {
        return this.baseBuilder(DEFAULT_TEMP, maxTokens);
    }

    protected ChatRequestBuilder baseBuilder(double temp) {
        return this.baseBuilder(temp, DEFAULT_MAX_TOKENS);
    }

    protected ChatRequestBuilder baseBuilder(double temp, int maxTokens) {
        return ChatRequest.builder()
                .model(this.model())
                .temperature(temp)
                .maxCompletionTokens(maxTokens);
    }

    protected String chat(ChatRequest chat) {
        return openAI.chatCompletions()
                .create(chat)
                .join()
                .firstContent();
    }

    private boolean shouldChoose(Choice choice) {
        return Optional.ofNullable(choice)
                .map(Choice::getMessage)
                .map(ResponseMessage::getContent)
                .filter(StringUtils::isNotEmpty)
                .isPresent();
    }

    private String extractToken(Chat chat) {
        return Optional.ofNullable(chat)
                .map(Chat::getChoices)
                .map(choices -> choices.stream()
                        .filter(this::shouldChoose)
                        .findFirst()
                        .orElse(null))
                .map(Choice::getMessage)
                .map(ResponseMessage::getContent)
                .orElse(null);
    }

    protected Stream<String> chatStream(ChatRequest chat) {
        var futureChat = openAI.chatCompletions().createStream(chat);
        var chatResponse = futureChat.join();
        return chatResponse.map(this::extractToken).filter(Objects::nonNull);
    }

}
