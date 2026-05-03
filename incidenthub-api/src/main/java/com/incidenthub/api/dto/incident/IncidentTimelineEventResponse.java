package com.incidenthub.api.dto.incident;

import com.incidenthub.core.domain.timeline.IncidentTimelineEventType;

import java.time.Instant;
import java.util.Map;

public record IncidentTimelineEventResponse(
        String eventId,
        String incidentId,
        IncidentTimelineEventType eventType,
        Instant occurredAt,
        String summary,
        String actor,
        Map<String, Object> attributes
) {
}