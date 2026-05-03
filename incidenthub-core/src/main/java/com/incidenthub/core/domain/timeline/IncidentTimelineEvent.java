package com.incidenthub.core.domain.timeline;

import com.incidenthub.core.domain.incident.IncidentId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record IncidentTimelineEvent(
        IncidentTimelineEventId id,
        IncidentId incidentId,
        IncidentTimelineEventType type,
        Instant occurredAt,
        String summary,
        String actor,
        Map<String, Object> attributes
) {

    public IncidentTimelineEvent {
        Objects.requireNonNull(id, "Incident timeline event id must not be null");
        Objects.requireNonNull(incidentId, "Incident id must not be null");
        Objects.requireNonNull(type, "Incident timeline event type must not be null");
        Objects.requireNonNull(occurredAt, "Occurred timestamp must not be null");
        summary = requireText(summary, "Timeline event summary must not be blank");
        actor = actor == null || actor.isBlank() ? "system" : actor.trim();
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static IncidentTimelineEvent record(
            IncidentId incidentId,
            IncidentTimelineEventType type,
            Instant occurredAt,
            String summary,
            String actor,
            Map<String, Object> attributes
    ) {
        return new IncidentTimelineEvent(
                IncidentTimelineEventId.newId(),
                incidentId,
                type,
                occurredAt,
                summary,
                actor,
                attributes
        );
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}