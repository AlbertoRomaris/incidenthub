package com.incidenthub.core.domain.signal;

import java.util.Objects;
import java.util.UUID;

public record SignalId(UUID value) {

    public SignalId {
        Objects.requireNonNull(value, "Signal id must not be null");
    }

    public static SignalId newId() {
        return new SignalId(UUID.randomUUID());
    }

    public static SignalId from(UUID value) {
        return new SignalId(value);
    }
}
