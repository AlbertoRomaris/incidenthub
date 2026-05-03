package com.incidenthub.core.domain.alert;

import java.util.Objects;
import java.util.UUID;

public record AlertId(UUID value) {

    public AlertId {
        Objects.requireNonNull(value, "Alert id must not be null");
    }

    public static AlertId newId() {
        return new AlertId(UUID.randomUUID());
    }

    public static AlertId from(UUID value) {
        return new AlertId(value);
    }
}