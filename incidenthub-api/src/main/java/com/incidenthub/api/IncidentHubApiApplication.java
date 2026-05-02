package com.incidenthub.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.incidenthub")
public class IncidentHubApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentHubApiApplication.class, args);
    }
}