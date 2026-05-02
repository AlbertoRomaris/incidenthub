package com.incidenthub.core.domain.evidence;

import java.util.Objects;
import java.util.UUID;

public record IncidentEvidenceId(UUID value) {

    public IncidentEvidenceId {
        Objects.requireNonNull(value, "Incident evidence id must not be null");
    }

    public static IncidentEvidenceId newId() {
        return new IncidentEvidenceId(UUID.randomUUID());
    }

    public static IncidentEvidenceId from(UUID value) {
        return new IncidentEvidenceId(value);
    }
}