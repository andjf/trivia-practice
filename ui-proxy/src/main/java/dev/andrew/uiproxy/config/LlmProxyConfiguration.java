package dev.andrew.uiproxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("llm-proxy")
public record LlmProxyConfiguration(String host) {
}
