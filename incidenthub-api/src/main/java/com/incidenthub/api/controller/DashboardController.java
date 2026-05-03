package com.incidenthub.api.controller;

import com.incidenthub.api.dto.dashboard.OperationalDashboardSummaryResponse;
import com.incidenthub.core.application.model.OperationalDashboardSummary;
import com.incidenthub.core.application.usecase.GetOperationalDashboardSummaryUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    private final GetOperationalDashboardSummaryUseCase getOperationalDashboardSummaryUseCase;

    public DashboardController(GetOperationalDashboardSummaryUseCase getOperationalDashboardSummaryUseCase) {
        this.getOperationalDashboardSummaryUseCase = getOperationalDashboardSummaryUseCase;
    }

    @GetMapping("/dashboard/summary")
    public OperationalDashboardSummaryResponse getSummary() {
        OperationalDashboardSummary summary = getOperationalDashboardSummaryUseCase.getSummary();

        return new OperationalDashboardSummaryResponse(
                summary.generatedAt(),
                summary.activeIncidents(),
                summary.openIncidents(),
                summary.acknowledgedIncidents(),
                summary.resolvedIncidents(),
                summary.criticalOpenIncidents(),
                summary.highOpenIncidents(),
                summary.pendingAlerts(),
                summary.sentAlerts(),
                summary.failedAlerts()
        );
    }
}