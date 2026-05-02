package com.incidenthub.core.domain.incident;

import java.time.Instant;
import java.util.Objects;

public record Incident(
        IncidentId id,
        String serviceName,
        String environment,
        IncidentType type,
        IncidentSeverity severity,
        IncidentStatus status,
        String summary,
        String description,
        DeduplicationKey deduplicationKey,
        int occurrenceCount,
        Instant firstSeenAt,
        Instant lastSeenAt,
        Instant openedAt,
        Instant acknowledgedAt,
        Instant resolvedAt
) {

    public Incident {
        Objects.requireNonNull(id, "Incident id must not be null");
        serviceName = requireText(serviceName, "Service name must not be blank");
        environment = requireText(environment, "Environment must not be blank");
        Objects.requireNonNull(type, "Incident type must not be null");
        Objects.requireNonNull(severity, "Incident severity must not be null");
        Objects.requireNonNull(status, "Incident status must not be null");
        summary = requireText(summary, "Incident summary must not be blank");
        description = description == null ? "" : description.trim();
        Objects.requireNonNull(deduplicationKey, "Deduplication key must not be null");

        if (occurrenceCount <= 0) {
            throw new IllegalArgumentException("Occurrence count must be greater than zero");
        }

        Objects.requireNonNull(firstSeenAt, "First seen timestamp must not be null");
        Objects.requireNonNull(lastSeenAt, "Last seen timestamp must not be null");
        Objects.requireNonNull(openedAt, "Opened timestamp must not be null");

        if (lastSeenAt.isBefore(firstSeenAt)) {
            throw new IllegalArgumentException("Last seen timestamp must not be before first seen timestamp");
        }
    }

    public static Incident open(
            String serviceName,
            String environment,
            IncidentType type,
            IncidentSeverity severity,
            String summary,
            String description,
            Instant openedAt
    ) {
        Objects.requireNonNull(openedAt, "Opened timestamp must not be null");

        return new Incident(
                IncidentId.newId(),
                serviceName,
                environment,
                type,
                severity,
                IncidentStatus.OPEN,
                summary,
                description,
                DeduplicationKey.from(environment, serviceName, type),
                1,
                openedAt,
                openedAt,
                openedAt,
                null,
                null
        );
    }

    public Incident recordOccurrence(Instant seenAt) {
        Objects.requireNonNull(seenAt, "Seen timestamp must not be null");

        if (status == IncidentStatus.RESOLVED) {
            throw new IllegalStateException("Cannot record occurrence on a resolved incident");
        }

        Instant updatedLastSeenAt = seenAt.isAfter(lastSeenAt) ? seenAt : lastSeenAt;

        return new Incident(
                id,
                serviceName,
                environment,
                type,
                severity,
                status,
                summary,
                description,
                deduplicationKey,
                occurrenceCount + 1,
                firstSeenAt,
                updatedLastSeenAt,
                openedAt,
                acknowledgedAt,
                resolvedAt
        );
    }

    public Incident acknowledge(Instant acknowledgedAt) {
        Objects.requireNonNull(acknowledgedAt, "Acknowledged timestamp must not be null");

        if (status != IncidentStatus.OPEN) {
            throw new IllegalStateException("Only open incidents can be acknowledged");
        }

        return new Incident(
                id,
                serviceName,
                environment,
                type,
                severity,
                IncidentStatus.ACKNOWLEDGED,
                summary,
                description,
                deduplicationKey,
                occurrenceCount,
                firstSeenAt,
                lastSeenAt,
                openedAt,
                acknowledgedAt,
                resolvedAt
        );
    }

    public Incident resolve(Instant resolvedAt) {
        Objects.requireNonNull(resolvedAt, "Resolved timestamp must not be null");

        if (status == IncidentStatus.RESOLVED) {
            throw new IllegalStateException("Incident is already resolved");
        }

        return new Incident(
                id,
                serviceName,
                environment,
                type,
                severity,
                IncidentStatus.RESOLVED,
                summary,
                description,
                deduplicationKey,
                occurrenceCount,
                firstSeenAt,
                lastSeenAt,
                openedAt,
                acknowledgedAt,
                resolvedAt
        );
    }

    public boolean isOpen() {
        return status == IncidentStatus.OPEN || status == IncidentStatus.ACKNOWLEDGED;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}