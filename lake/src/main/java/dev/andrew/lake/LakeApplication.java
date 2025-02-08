package dev.andrew.lake;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import dev.andrew.lake.config.OpenAiConfiguration;

@SpringBootApplication
@EnableConfigurationProperties(OpenAiConfiguration.class)
public class LakeApplication {

	public static void main(String[] args) {
		SpringApplication.run(LakeApplication.class, args);
	}

}
