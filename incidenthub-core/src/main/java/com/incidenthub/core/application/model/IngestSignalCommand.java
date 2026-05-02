package com.incidenthub.core.application.model;

import com.incidenthub.core.domain.signal.SignalSeverity;
import com.incidenthub.core.domain.signal.SignalType;

import java.time.Instant;
import java.util.Map;

public record IngestSignalCommand(
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
        Map<String, Object> attributes
) {
}