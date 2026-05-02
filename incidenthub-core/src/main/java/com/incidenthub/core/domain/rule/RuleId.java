package com.incidenthub.core.domain.rule;

import java.util.Objects;
import java.util.UUID;

public record RuleId(UUID value) {

    public RuleId {
        Objects.requireNonNull(value, "Rule id must not be null");
    }

    public static RuleId newId() {
        return new RuleId(UUID.randomUUID());
    }

    public static RuleId from(UUID value) {
        return new RuleId(value);
    }
}
