package com.incidenthub.infrastructure.persistence.incident;

import com.incidenthub.core.domain.incident.*;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incidents")
public class IncidentEntity {

    @Id
    private UUID id;

    @Column(name = "service_name", nullable = false, length = 120)
    private String serviceName;

    @Column(name = "environment", nullable = false, length = 60)
    private String environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false, length = 80)
    private IncidentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 40)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private IncidentStatus status;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "deduplication_key", nullable = false, length = 260)
    private String deduplicationKey;

    @Column(name = "occurrence_count", nullable = false)
    private int occurrenceCount;

    @Column(name = "first_seen_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant firstSeenAt;

    @Column(name = "last_seen_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant lastSeenAt;

    @Column(name = "opened_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant openedAt;

    @Column(name = "acknowledged_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant acknowledgedAt;

    @Column(name = "resolved_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant resolvedAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant updatedAt;

    protected IncidentEntity() {
        // Required by JPA
    }

    private IncidentEntity(
            UUID id,
            String serviceName,
            String environment,
            IncidentType type,
            IncidentSeverity severity,
            IncidentStatus status,
            String summary,
            String description,
            String deduplicationKey,
            int occurrenceCount,
            Instant firstSeenAt,
            Instant lastSeenAt,
            Instant openedAt,
            Instant acknowledgedAt,
            Instant resolvedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.serviceName = serviceName;
        this.environment = environment;
        this.type = type;
        this.severity = severity;
        this.status = status;
        this.summary = summary;
        this.description = description;
        this.deduplicationKey = deduplicationKey;
        this.occurrenceCount = occurrenceCount;
        this.firstSeenAt = firstSeenAt;
        this.lastSeenAt = lastSeenAt;
        this.openedAt = openedAt;
        this.acknowledgedAt = acknowledgedAt;
        this.resolvedAt = resolvedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static IncidentEntity fromDomain(Incident incident) {
        Instant now = Instant.now();

        return new IncidentEntity(
                incident.id().value(),
                incident.serviceName(),
                incident.environment(),
                incident.type(),
                incident.severity(),
                incident.status(),
                incident.summary(),
                incident.description(),
                incident.deduplicationKey().value(),
                incident.occurrenceCount(),
                incident.firstSeenAt(),
                incident.lastSeenAt(),
                incident.openedAt(),
                incident.acknowledgedAt(),
                incident.resolvedAt(),
                now,
                now
        );
    }

    public Incident toDomain() {
        return new Incident(
                IncidentId.from(id),
                serviceName,
                environment,
                type,
                severity,
                status,
                summary,
                description,
                new DeduplicationKey(deduplicationKey),
                occurrenceCount,
                firstSeenAt,
                lastSeenAt,
                openedAt,
                acknowledgedAt,
                resolvedAt
        );
    }

    public void preserveCreatedAtFrom(IncidentEntity existingEntity) {
        this.createdAt = existingEntity.createdAt;
    }
}