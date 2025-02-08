package dev.andrew.lake.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HealthController {

    @Value("${spring.application.name}")
    String applicationName;

    @GetMapping(value = { "/", "/health" })
    public String getMethodName() {
        return applicationName + " OK";
    }

}
