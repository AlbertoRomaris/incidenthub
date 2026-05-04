package com.incidenthub.api.dto.metrics;

import java.time.Instant;
import java.util.List;

public record OperationalMetricsResponse(
        Instant generatedAt,
        List<OperationalMetricResponse> metrics
) {
}