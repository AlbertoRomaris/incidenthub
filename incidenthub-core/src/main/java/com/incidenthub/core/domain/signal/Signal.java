package com.incidenthub.core.domain.signal;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record Signal(
        SignalId id,
        String serviceName,
        String environment,
        SignalType type,
        SignalSeverity severity,
        String message,
        String correlationId,
        String traceId,
        String spanId,
        Integer latencyMs,
        Integer statusCode,
        String errorCode,
        Instant observedAt,
        Instant receivedAt,
        Map<String, Object> attributes
) {

    public Signal {
        Objects.requireNonNull(id, "Signal id must not be null");
        serviceName = requireText(serviceName, "Service name must not be blank");
        environment = requireText(environment, "Environment must not be blank");
        Objects.requireNonNull(type, "Signal type must not be null");
        Objects.requireNonNull(severity, "Signal severity must not be null");
        message = requireText(message, "Message must not be blank");
        Objects.requireNonNull(observedAt, "Observed timestamp must not be null");
        Objects.requireNonNull(receivedAt, "Received timestamp must not be null");

        if (latencyMs != null && latencyMs < 0) {
            throw new IllegalArgumentException("Latency must not be negative");
        }

        if (statusCode != null && (statusCode < 100 || statusCode > 599)) {
            throw new IllegalArgumentException("HTTP status code must be between 100 and 599");
        }

        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static Signal received(
            String serviceName,
            String environment,
            SignalType type,
            SignalSeverity severity,
            String message,
            String correlationId,
            String traceId,
            String spanId,
            Integer latencyMs,
            Integer statusCode,
            String errorCode,
            Instant observedAt,
            Instant receivedAt,
            Map<String, Object> attributes
    ) {
        return new Signal(
                SignalId.newId(),
                serviceName,
                environment,
                type,
                severity,
                message,
                correlationId,
                traceId,
                spanId,
                latencyMs,
                statusCode,
                errorCode,
                observedAt,
                receivedAt,
                attributes
        );
    }

    public boolean hasCorrelationId() {
        return correlationId != null && !correlationId.isBlank();
    }

    public boolean isErrorLike() {
        return type == SignalType.ERROR
                || type == SignalType.TIMEOUT
                || type == SignalType.DEPENDENCY_FAILURE;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}