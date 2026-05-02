package com.incidenthub.core.domain.incident;

import java.util.Objects;
import java.util.UUID;

public record IncidentId(UUID value) {

    public IncidentId {
        Objects.requireNonNull(value, "Incident id must not be null");
    }

    public static IncidentId newId() {
        return new IncidentId(UUID.randomUUID());
    }

    public static IncidentId from(UUID value) {
        return new IncidentId(value);
    }
}