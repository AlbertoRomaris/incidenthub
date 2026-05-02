package com.incidenthub.core.domain.incident;

import java.util.Locale;

public record DeduplicationKey(String value) {

    public DeduplicationKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Deduplication key must not be blank");
        }

        value = value.trim().toLowerCase(Locale.ROOT);
    }

    public static DeduplicationKey from(
            String environment,
            String serviceName,
            IncidentType incidentType
    ) {
        String normalizedEnvironment = requireText(environment, "Environment must not be blank");
        String normalizedServiceName = requireText(serviceName, "Service name must not be blank");

        if (incidentType == null) {
            throw new IllegalArgumentException("Incident type must not be null");
        }

        return new DeduplicationKey(
                normalizedEnvironment + ":" + normalizedServiceName + ":" + incidentType.name()
        );
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}