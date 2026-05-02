package com.incidenthub.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.incidenthub")
@EntityScan(basePackages = "com.incidenthub.infrastructure.persistence")
@EnableJpaRepositories(basePackages = "com.incidenthub.infrastructure.persistence")
public class IncidentHubApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentHubApiApplication.class, args);
    }
}