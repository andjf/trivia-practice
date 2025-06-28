package dev.andrew.llmproxy.function;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import dev.andrew.llmproxy.config.OpenAiConfiguration;
import dev.andrew.llmproxy.util.SpringContext;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.function.Functional;
import io.github.sashirestela.openai.domain.chat.ChatMessage.SystemMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import io.github.sashirestela.openai.domain.chat.ChatRequest.ChatRequestBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@JsonClassDescription("Get the birth and possible death date of any individual mentioned")
public class LifeFunction implements Functional {

    protected OpenAiConfiguration openAiConfiguration;
    protected SimpleOpenAI openAI;

    @JsonPropertyDescription("The first and last name of the individual to get the birthday of. Example: Frank Sinatra")
    @JsonProperty(required = true)
    public String name;

    private void initialize() {
        if (openAI == null) {
            this.openAiConfiguration = SpringContext.getBean(OpenAiConfiguration.class);
            this.openAI = SimpleOpenAI.builder()
                    .apiKey(this.openAiConfiguration.apiKey())
                    .build();
        }
    }

    private ChatRequestBuilder baseBuilder(double temp, int maxTokens) {
        return ChatRequest.builder()
                .model(this.openAiConfiguration.model())
                .temperature(temp)
                .maxCompletionTokens(maxTokens);
    }

    private String chat(ChatRequest chat) {
        return openAI.chatCompletions()
                .create(chat)
                .join()
                .firstContent();
    }

    @Override
    public String execute() {
        initialize();

        String prompt = "When was " + name + " born and when did they die (if applicable)?";

        ChatRequest chat = baseBuilder(0.5, 100)
                .message(SystemMessage.of("You are an expert in historical dates."))
                .message(UserMessage.of(prompt))
                .build();

        String response = this.chat(chat);

        log.info("prompt=[{}] response=[{}]", prompt, response);

        return response;
    }
}
