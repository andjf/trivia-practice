package dev.andrew.uiproxy.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HealthController {

    @Value("${spring.application.name}")
    String applicationName;

    @GetMapping(value = { "/", "/health" }, produces = MediaType.TEXT_PLAIN_VALUE)
    public String getMethodName() {
        return applicationName + " OK";
    }

}
