package com.incidenthub.core.domain.metrics;

import java.time.Instant;
import java.util.Objects;

public record SloEvaluation(
        String key,
        String description,
        OperationalMetricName metricName,
        double actualValue,
        double targetValue,
        OperationalMetricUnit unit,
        SloStatus status,
        Instant evaluatedAt
) {

    public SloEvaluation {
        key = requireText(key, "SLO key must not be blank");
        description = requireText(description, "SLO description must not be blank");
        Objects.requireNonNull(metricName, "Metric name must not be null");
        Objects.requireNonNull(unit, "Metric unit must not be null");
        Objects.requireNonNull(status, "SLO status must not be null");
        Objects.requireNonNull(evaluatedAt, "Evaluated timestamp must not be null");

        if (!Double.isFinite(actualValue)) {
            throw new IllegalArgumentException("Actual value must be finite");
        }

        if (!Double.isFinite(targetValue)) {
            throw new IllegalArgumentException("Target value must be finite");
        }

        if (actualValue < 0 || targetValue < 0) {
            throw new IllegalArgumentException("SLO values must not be negative");
        }
    }

    public boolean healthy() {
        return status == SloStatus.HEALTHY;
    }

    public boolean breached() {
        return status == SloStatus.BREACHED;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}