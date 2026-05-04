package com.incidenthub.api.dto.metrics;

import com.incidenthub.core.domain.metrics.OperationalMetricName;
import com.incidenthub.core.domain.metrics.OperationalMetricUnit;
import com.incidenthub.core.domain.metrics.SloStatus;

import java.time.Instant;

public record SloEvaluationResponse(
        String key,
        String description,
        OperationalMetricName metricName,
        double actualValue,
        double targetValue,
        OperationalMetricUnit unit,
        SloStatus status,
        boolean healthy,
        boolean breached,
        Instant evaluatedAt
) {
}
