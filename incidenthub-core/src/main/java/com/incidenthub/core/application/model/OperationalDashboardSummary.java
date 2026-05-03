package com.incidenthub.core.application.model;

import java.time.Instant;
import java.util.Objects;

public record OperationalDashboardSummary(
        Instant generatedAt,
        long activeIncidents,
        long openIncidents,
        long acknowledgedIncidents,
        long resolvedIncidents,
        long criticalOpenIncidents,
        long highOpenIncidents,
        long pendingAlerts,
        long sentAlerts,
        long failedAlerts
) {

    public OperationalDashboardSummary {
        Objects.requireNonNull(generatedAt, "Generated timestamp must not be null");
    }
}