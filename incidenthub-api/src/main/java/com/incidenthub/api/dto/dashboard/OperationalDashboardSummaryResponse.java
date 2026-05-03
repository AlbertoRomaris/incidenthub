package com.incidenthub.api.dto.dashboard;

import java.time.Instant;

public record OperationalDashboardSummaryResponse(
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
}