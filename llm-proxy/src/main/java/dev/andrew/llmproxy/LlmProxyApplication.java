package dev.andrew.llmproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import dev.andrew.llmproxy.config.OpenAiConfiguration;

@SpringBootApplication
@EnableConfigurationProperties(OpenAiConfiguration.class)
public class LlmProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(LlmProxyApplication.class, args);
	}

}
