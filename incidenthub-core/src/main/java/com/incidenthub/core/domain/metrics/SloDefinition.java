package com.incidenthub.core.domain.metrics;

import java.time.Instant;
import java.util.Objects;

public record SloDefinition(
        String key,
        String description,
        OperationalMetricName metricName,
        SloComparison comparison,
        double target,
        OperationalMetricUnit unit
) {

    public SloDefinition {
        key = requireText(key, "SLO key must not be blank");
        description = requireText(description, "SLO description must not be blank");
        Objects.requireNonNull(metricName, "SLO metric name must not be null");
        Objects.requireNonNull(comparison, "SLO comparison must not be null");
        Objects.requireNonNull(unit, "SLO unit must not be null");

        if (!Double.isFinite(target)) {
            throw new IllegalArgumentException("SLO target must be finite");
        }

        if (target < 0) {
            throw new IllegalArgumentException("SLO target must not be negative");
        }

        if (unit == OperationalMetricUnit.PERCENT && target > 100) {
            throw new IllegalArgumentException("SLO percentage target must not be greater than 100");
        }
    }

    public SloEvaluation evaluate(OperationalMetric metric, Instant evaluatedAt) {
        Objects.requireNonNull(metric, "Operational metric must not be null");
        Objects.requireNonNull(evaluatedAt, "Evaluation timestamp must not be null");

        if (metric.name() != metricName) {
            throw new IllegalArgumentException("Metric does not match SLO definition");
        }

        boolean satisfied = comparison.isSatisfied(metric.value(), target);

        return new SloEvaluation(
                key,
                description,
                metric.name(),
                metric.value(),
                target,
                unit,
                satisfied ? SloStatus.HEALTHY : SloStatus.BREACHED,
                evaluatedAt
        );
    }

    public static SloDefinition signalProcessingSuccessRate() {
        return new SloDefinition(
                "signal-processing-success-rate",
                "At least 99% of signal processing tasks should complete successfully.",
                OperationalMetricName.SIGNAL_PROCESSING_SUCCESS_RATE,
                SloComparison.GREATER_THAN_OR_EQUAL,
                99.0,
                OperationalMetricUnit.PERCENT
        );
    }

    public static SloDefinition alertDeliverySuccessRate() {
        return new SloDefinition(
                "alert-delivery-success-rate",
                "At least 99% of alerts should be delivered successfully.",
                OperationalMetricName.ALERT_DELIVERY_SUCCESS_RATE,
                SloComparison.GREATER_THAN_OR_EQUAL,
                99.0,
                OperationalMetricUnit.PERCENT
        );
    }

    public static SloDefinition pendingTaskBacklog() {
        return new SloDefinition(
                "pending-task-backlog",
                "Pending signal processing tasks should stay below 50.",
                OperationalMetricName.SIGNAL_PROCESSING_TASKS_PENDING,
                SloComparison.LESS_THAN_OR_EQUAL,
                50.0,
                OperationalMetricUnit.COUNT
        );
    }

    public static SloDefinition oldestPendingTaskAge() {
        return new SloDefinition(
                "oldest-pending-task-age",
                "The oldest pending task should be younger than 60 seconds.",
                OperationalMetricName.OLDEST_PENDING_TASK_AGE_SECONDS,
                SloComparison.LESS_THAN_OR_EQUAL,
                60.0,
                OperationalMetricUnit.SECONDS
        );
    }

    public static SloDefinition signalProcessingAverageLatency() {
        return new SloDefinition(
                "signal-processing-average-latency",
                "Average signal processing latency should stay below 10 seconds.",
                OperationalMetricName.SIGNAL_PROCESSING_AVERAGE_LATENCY_MS,
                SloComparison.LESS_THAN_OR_EQUAL,
                10_000.0,
                OperationalMetricUnit.MILLISECONDS
        );
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}