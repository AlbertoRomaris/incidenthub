package com.incidenthub.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.incidenthub")
public class IncidentHubWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentHubWorkerApplication.class, args);
    }
}