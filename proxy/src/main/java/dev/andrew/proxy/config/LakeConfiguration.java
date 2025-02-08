package dev.andrew.proxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("lake")
public record LakeConfiguration(String host) {
}
