package dev.andrew.uiproxy.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import dev.andrew.uiproxy.config.LlmProxyConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class LlmProxyController {

    private static final String STANDARD_CHAT_RESOURCE = "/v1/chat";
    private static final String STREAMING_CHAT_RESOURCE = "/v1/chat/stream";

    private final LlmProxyConfiguration llmProxyConfig;
    private final WebClient llmProxyWebClient;

    public LlmProxyController(LlmProxyConfiguration llmproxyConfig, WebClient.Builder builder) {
        this.llmProxyConfig = llmproxyConfig;
        this.llmProxyWebClient = builder.baseUrl(this.llmProxyConfig.host()).build();
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody String prompt) {
        return this.llmProxyWebClient.post()
                .uri(STREAMING_CHAT_RESOURCE)
                .body(Mono.just(prompt), String.class)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class);
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> chat(@RequestBody String prompt) {
        return this.llmProxyWebClient.post()
                .uri(STANDARD_CHAT_RESOURCE)
                .body(Mono.just(prompt), String.class)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToMono(String.class);
    }

}
