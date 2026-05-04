package com.incidenthub.api.dto.metrics;

import com.incidenthub.core.domain.metrics.OperationalMetricName;
import com.incidenthub.core.domain.metrics.OperationalMetricType;
import com.incidenthub.core.domain.metrics.OperationalMetricUnit;

import java.time.Instant;
import java.util.Map;

public record OperationalMetricResponse(
        OperationalMetricName name,
        OperationalMetricType type,
        OperationalMetricUnit unit,
        double value,
        Instant measuredAt,
        Map<String, Object> attributes
) {
}
