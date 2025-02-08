package dev.andrew.lake.controller;

import java.util.Objects;
import java.util.stream.Stream;

import dev.andrew.lake.config.OpenAiConfiguration;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import io.github.sashirestela.openai.domain.chat.ChatRequest.ChatRequestBuilder;

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

    protected Stream<String> chatStream(ChatRequest chat) {
        var futureChat = openAI.chatCompletions().createStream(chat);
        var chatResponse = futureChat.join();
        return chatResponse.filter(res -> res.getChoices().size() > 0)
                .map(Chat::firstContent)
                .filter(Objects::nonNull);
    }

}
