package dev.andrew.uiproxy.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import dev.andrew.uiproxy.config.LlmProxyConfiguration;
import dev.andrew.uiproxy.model.TriviaQuestion;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/trivia")
public class TriviaController {

    private static final String NEW_TRIVIA_RESOURCE = "/trivia/new";

    private final LlmProxyConfiguration llmProxyConfig;
    private final WebClient llmProxyWebClient;

    public TriviaController(LlmProxyConfiguration llmproxyConfig, WebClient.Builder builder) {
        this.llmProxyConfig = llmproxyConfig;
        this.llmProxyWebClient = builder.baseUrl(this.llmProxyConfig.host()).build();
    }

    @GetMapping(value = "/new", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TriviaQuestion> newTriviaQuestion(@RequestParam String topic, @RequestParam String difficulty) {
        return this.llmProxyWebClient.get()
                .uri(uriBuilder -> uriBuilder.path(NEW_TRIVIA_RESOURCE)
                        .queryParam("topic", topic)
                        .queryParam("difficulty", difficulty)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TriviaQuestion.class);
    }

}
