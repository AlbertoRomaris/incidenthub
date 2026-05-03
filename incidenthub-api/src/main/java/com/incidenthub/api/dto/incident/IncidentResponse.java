package com.incidenthub.api.dto.incident;

import com.incidenthub.core.domain.incident.IncidentSeverity;
import com.incidenthub.core.domain.incident.IncidentStatus;
import com.incidenthub.core.domain.incident.IncidentType;

import java.time.Instant;

public record IncidentResponse(
        String incidentId,
        String serviceName,
        String environment,
        IncidentType incidentType,
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
        Instant resolvedAt
) {
}