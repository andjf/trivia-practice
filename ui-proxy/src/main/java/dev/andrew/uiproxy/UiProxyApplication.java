package dev.andrew.uiproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import dev.andrew.uiproxy.config.LlmProxyConfiguration;

@SpringBootApplication
@EnableConfigurationProperties(LlmProxyConfiguration.class)
public class UiProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(UiProxyApplication.class, args);
	}

}
