package dev.andrew.llmproxy.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.andrew.llmproxy.config.OpenAiConfiguration;
import dev.andrew.llmproxy.function.LifeFunction;
import io.github.sashirestela.openai.common.function.FunctionCall;
import io.github.sashirestela.openai.common.function.FunctionDef;
import io.github.sashirestela.openai.common.function.FunctionExecutor;
import io.github.sashirestela.openai.common.tool.ToolCall;
import io.github.sashirestela.openai.common.tool.ToolChoiceOption;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.Chat.Choice;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.AssistantMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.ResponseMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.SystemMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.ToolMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Slf4j
@RestController
public class ChatController extends AiController {

    private final FunctionExecutor fx;

    public ChatController(OpenAiConfiguration openAiConfiguration) {
        super(openAiConfiguration);
        this.fx = new FunctionExecutor();
        this.fx.enrollFunction(FunctionDef.of(LifeFunction.class));
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

    private void printEncodedStreamingToken(String token) {
        try {
            log.info("(encoded) streaming response token=[{}]",
                    URLEncoder.encode(token, StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException e) {
            log.error("failed to url encode token", e);
        }
    }

    @PostMapping(value = "/v1/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody String prompt) {
        List<ChatMessage> messages = List.of(
                SystemMessage.of("You are an expert AI assistant designed to answer questions."),
                UserMessage.of(prompt));

        return runStream(messages)
                .doOnNext(this::printEncodedStreamingToken)
                .map(token -> new JSONObject(Map.of("token", token)))
                .map(JSONObject::toString);
    }

    private Flux<String> runStream(List<ChatMessage> messages) {
        log.info("Creating streaming response with messages [{}]",
                messages.stream()
                        .map(ChatController::json)
                        .collect(Collectors.joining(", ")));

        ChatRequest chatRequest = baseBuilder()
                .messages(messages)
                .tools(fx.getToolFunctions())
                .toolChoice(ToolChoiceOption.AUTO)
                .stream(true)
                .build();

        return Flux.create(sink -> {
            CompletableFuture<Void> future = openAI.chatCompletions()
                    .createStream(chatRequest)
                    .thenAccept(tokenStreamConsumer(sink, messages))
                    .exceptionally(err -> {
                        sink.error(err);
                        return null;
                    });

            sink.onCancel(() -> future.cancel(true));
        });
    }

    private Consumer<Stream<Chat>> tokenStreamConsumer(
            FluxSink<String> sink,
            List<ChatMessage> previousMessages) {

        return stream -> {
            List<ToolCall> toolCalls = new ArrayList<ToolCall>();
            stream.map(Chat::getChoices)
                    .map(CollectionUtils::firstElement)
                    .filter(Objects::nonNull)
                    .forEach(tokenFirstChoiceConsumer(sink, previousMessages, toolCalls));
        };
    }

    private Consumer<Choice> tokenFirstChoiceConsumer(
            FluxSink<String> sink,
            List<ChatMessage> previousMessages,
            List<ToolCall> toolCalls) {

        return choice -> {
            ResponseMessage delta = choice.getMessage();

            // Stream normal contnet tokens back
            Optional.ofNullable(delta.getContent())
                    .ifPresent(sink::next);

            // Process disperate tool_call tokens
            Optional.ofNullable(delta.getToolCalls())
                    .map(CollectionUtils::firstElement)
                    .ifPresent(toolCallTokenConsumer(toolCalls));

            // Process finish reason (if present)
            Optional.ofNullable(choice.getFinishReason())
                    .filter(StringUtils::isNotBlank)
                    .ifPresent(finishReasonConsumer(sink, previousMessages, toolCalls));
        };
    }

    private Consumer<ToolCall> toolCallTokenConsumer(List<ToolCall> toolCalls) {

        return newToolCall -> {
            ToolCall currentToolCall = CollectionUtils.lastElement(toolCalls);

            // If this is the first tool_call token from the first took call or
            // if this is the first tool_call token from the non-first token
            // this is now the most recent tool call
            if (currentToolCall == null || currentToolCall.getIndex() != newToolCall.getIndex()) {
                toolCalls.add(newToolCall);
                return;
            }

            // otherwise, we want to append the arguments from the new tool_call
            // to the "current" tool call being build (the last element of toolCalls)
            FunctionCall currentFunctionCall = currentToolCall.getFunction();
            String currentArguments = currentFunctionCall.getArguments();

            FunctionCall newFunctionCall = newToolCall.getFunction();
            String newArguments = newFunctionCall.getArguments();

            currentFunctionCall.setArguments(currentArguments + newArguments);
        };
    }

    private Consumer<String> finishReasonConsumer(
            FluxSink<String> sink,
            List<ChatMessage> previousMessages,
            List<ToolCall> toolCalls) {

        return finishReason -> {
            switch (finishReason) {
                case "tool_calls":
                    reinitiateConversation(sink, previousMessages, toolCalls);
                    break;
                case "stop":
                    sink.complete();
                    break;
                default:
                    log.warn("unrecognized finish reason=[{}]", finishReason);
            }
        };
    }

    private void reinitiateConversation(
            FluxSink<String> sink,
            List<ChatMessage> previousMessages,
            List<ToolCall> toolCalls) {

        List<ChatMessage> followupMessages = new ArrayList<>(previousMessages);
        followupMessages.add(AssistantMessage.of(toolCalls));
        followupMessages.addAll(executeToolCalls(toolCalls));

        runStream(followupMessages).subscribe(
                sink::next,
                sink::error,
                sink::complete);
    }

    private List<ToolMessage> executeToolCalls(Collection<ToolCall> toolCalls) {
        return toolCalls.stream()
                .parallel() // reconsider this
                .unordered()
                .map(this::executeToolCall)
                .toList();
    }

    private ToolMessage executeToolCall(ToolCall toolCall) {
        return ToolMessage.of(fx.execute(toolCall.getFunction()).toString(), toolCall.getId());
    }

    private static String json(Object o) {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

}
