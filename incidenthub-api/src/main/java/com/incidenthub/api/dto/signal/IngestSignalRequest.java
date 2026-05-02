package com.incidenthub.api.dto.signal;

import com.incidenthub.core.domain.signal.SignalSeverity;
import com.incidenthub.core.domain.signal.SignalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

public record IngestSignalRequest(

        @NotBlank
        String serviceName,

        @NotBlank
        String environment,

        @NotNull
        SignalType signalType,

        @NotNull
        SignalSeverity severity,

        @NotBlank
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