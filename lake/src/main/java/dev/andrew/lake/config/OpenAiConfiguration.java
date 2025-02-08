package dev.andrew.lake.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("openai")
public record OpenAiConfiguration(String model, String apiKey) {
}
