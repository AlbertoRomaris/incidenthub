package com.incidenthub.core.domain.metrics;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record OperationalMetric(
        OperationalMetricName name,
        OperationalMetricType type,
        OperationalMetricUnit unit,
        double value,
        Instant measuredAt,
        Map<String, Object> attributes
) {

    public OperationalMetric {
        Objects.requireNonNull(name, "Operational metric name must not be null");
        Objects.requireNonNull(type, "Operational metric type must not be null");
        Objects.requireNonNull(unit, "Operational metric unit must not be null");
        Objects.requireNonNull(measuredAt, "Measured timestamp must not be null");

        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Operational metric value must be finite");
        }

        if (value < 0) {
            throw new IllegalArgumentException("Operational metric value must not be negative");
        }

        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static OperationalMetric counter(
            OperationalMetricName name,
            double value,
            Instant measuredAt
    ) {
        return new OperationalMetric(
                name,
                OperationalMetricType.COUNTER,
                OperationalMetricUnit.COUNT,
                value,
                measuredAt,
                Map.of()
        );
    }

    public static OperationalMetric gauge(
            OperationalMetricName name,
            OperationalMetricUnit unit,
            double value,
            Instant measuredAt
    ) {
        return new OperationalMetric(
                name,
                OperationalMetricType.GAUGE,
                unit,
                value,
                measuredAt,
                Map.of()
        );
    }

    public static OperationalMetric ratio(
            OperationalMetricName name,
            double percentage,
            Instant measuredAt
    ) {
        if (percentage > 100) {
            throw new IllegalArgumentException("Ratio metric percentage must not be greater than 100");
        }

        return new OperationalMetric(
                name,
                OperationalMetricType.RATIO,
                OperationalMetricUnit.PERCENT,
                percentage,
                measuredAt,
                Map.of()
        );
    }

    public OperationalMetric withAttributes(Map<String, Object> attributes) {
        return new OperationalMetric(
                name,
                type,
                unit,
                value,
                measuredAt,
                attributes
        );
    }
}