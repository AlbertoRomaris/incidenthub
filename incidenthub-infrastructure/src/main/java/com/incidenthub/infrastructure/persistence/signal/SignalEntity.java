package com.incidenthub.infrastructure.persistence.signal;

import com.incidenthub.core.domain.signal.Signal;
import com.incidenthub.core.domain.signal.SignalId;
import com.incidenthub.core.domain.signal.SignalSeverity;
import com.incidenthub.core.domain.signal.SignalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "signals")
public class SignalEntity {

    @Id
    private UUID id;

    @Column(name = "service_name", nullable = false, length = 120)
    private String serviceName;

    @Column(name = "environment", nullable = false, length = 60)
    private String environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "signal_type", nullable = false, length = 60)
    private SignalType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 40)
    private SignalSeverity severity;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "correlation_id", length = 120)
    private String correlationId;

    @Column(name = "trace_id", length = 120)
    private String traceId;

    @Column(name = "span_id", length = 120)
    private String spanId;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "error_code", length = 120)
    private String errorCode;

    @Column(name = "observed_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant observedAt;

    @Column(name = "received_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant receivedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> attributes = new HashMap<>();

    protected SignalEntity() {
        // Required by JPA
    }

    private SignalEntity(
            UUID id,
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
        this.id = id;
        this.serviceName = serviceName;
        this.environment = environment;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.correlationId = correlationId;
        this.traceId = traceId;
        this.spanId = spanId;
        this.latencyMs = latencyMs;
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.observedAt = observedAt;
        this.receivedAt = receivedAt;
        this.attributes = attributes == null ? new HashMap<>() : new HashMap<>(attributes);
    }

    public static SignalEntity fromDomain(Signal signal) {
        return new SignalEntity(
                signal.id().value(),
                signal.serviceName(),
                signal.environment(),
                signal.type(),
                signal.severity(),
                signal.message(),
                signal.correlationId(),
                signal.traceId(),
                signal.spanId(),
                signal.latencyMs(),
                signal.statusCode(),
                signal.errorCode(),
                signal.observedAt(),
                signal.receivedAt(),
                signal.attributes()
        );
    }

    public Signal toDomain() {
        return new Signal(
                SignalId.from(id),
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
}