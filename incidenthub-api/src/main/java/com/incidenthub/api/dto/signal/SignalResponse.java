package com.incidenthub.api.dto.signal;

import com.incidenthub.core.domain.signal.SignalSeverity;
import com.incidenthub.core.domain.signal.SignalType;

import java.time.Instant;
import java.util.Map;

public record SignalResponse(
        String signalId,
        String serviceName,
        String environment,
        SignalType signalType,
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
}