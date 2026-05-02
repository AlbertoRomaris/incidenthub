package com.incidenthub.demo.payment.client;

import java.time.Instant;
import java.util.Map;

public record IncidentHubSignalRequest(
        String serviceName,
        String environment,
        String signalType,
        String severity,
        String message,
        String correlationId,
        String traceId,
        String spanId,
        Integer latencyMs,
        Integer statusCode,
        String errorCode,
        Instant timestamp,
        Map<String, Object> attributes
) {
}