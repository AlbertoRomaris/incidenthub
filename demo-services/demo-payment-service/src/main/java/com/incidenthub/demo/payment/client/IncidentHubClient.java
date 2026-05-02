package com.incidenthub.demo.payment.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class IncidentHubClient {

    private static final Logger log = LoggerFactory.getLogger(IncidentHubClient.class);

    private final RestClient restClient;

    public IncidentHubClient(
            RestClient.Builder restClientBuilder,
            @Value("${incidenthub.api.base-url:http://localhost:8080}") String incidentHubBaseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(incidentHubBaseUrl)
                .build();
    }

    public void publishSignal(IncidentHubSignalRequest request) {
        try {
            restClient.post()
                    .uri("/signals")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Published operational signal to IncidentHub: service={}, type={}, severity={}, correlationId={}",
                    request.serviceName(),
                    request.signalType(),
                    request.severity(),
                    request.correlationId());
        } catch (RestClientException exception) {
            log.warn("Failed to publish operational signal to IncidentHub: {}", exception.getMessage());
        }
    }
}