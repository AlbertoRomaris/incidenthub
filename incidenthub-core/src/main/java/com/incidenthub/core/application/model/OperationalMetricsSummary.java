package com.incidenthub.core.application.model;

import com.incidenthub.core.domain.metrics.OperationalMetric;
import com.incidenthub.core.domain.metrics.SloEvaluation;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record OperationalMetricsSummary(
        Instant generatedAt,
        List<OperationalMetric> metrics,
        List<SloEvaluation> sloEvaluations
) {

    public OperationalMetricsSummary {
        Objects.requireNonNull(generatedAt, "Generated timestamp must not be null");
        metrics = metrics == null ? List.of() : List.copyOf(metrics);
        sloEvaluations = sloEvaluations == null ? List.of() : List.copyOf(sloEvaluations);
    }
}