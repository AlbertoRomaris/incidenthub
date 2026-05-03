package com.incidenthub.core.domain.timeline;

import java.util.Objects;
import java.util.UUID;

public record IncidentTimelineEventId(UUID value) {

    public IncidentTimelineEventId {
        Objects.requireNonNull(value, "Incident timeline event id must not be null");
    }

    public static IncidentTimelineEventId newId() {
        return new IncidentTimelineEventId(UUID.randomUUID());
    }

    public static IncidentTimelineEventId from(UUID value) {
        return new IncidentTimelineEventId(value);
    }
}