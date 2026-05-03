package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.model.OperationalDashboardSummary;
import com.incidenthub.core.application.port.AlertRepository;
import com.incidenthub.core.application.port.IncidentRepository;
import com.incidenthub.core.domain.alert.AlertStatus;
import com.incidenthub.core.domain.incident.IncidentSeverity;
import com.incidenthub.core.domain.incident.IncidentStatus;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class GetOperationalDashboardSummaryUseCase {

    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final Clock clock;

    public GetOperationalDashboardSummaryUseCase(
            IncidentRepository incidentRepository,
            AlertRepository alertRepository,
            Clock clock
    ) {
        this.incidentRepository = Objects.requireNonNull(incidentRepository, "Incident repository must not be null");
        this.alertRepository = Objects.requireNonNull(alertRepository, "Alert repository must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    public OperationalDashboardSummary getSummary() {
        long openIncidents = incidentRepository.countByStatus(IncidentStatus.OPEN);
        long acknowledgedIncidents = incidentRepository.countByStatus(IncidentStatus.ACKNOWLEDGED);

        return new OperationalDashboardSummary(
                Instant.now(clock),
                openIncidents + acknowledgedIncidents,
                openIncidents,
                acknowledgedIncidents,
                incidentRepository.countByStatus(IncidentStatus.RESOLVED),
                incidentRepository.countByStatusAndSeverity(IncidentStatus.OPEN, IncidentSeverity.CRITICAL),
                incidentRepository.countByStatusAndSeverity(IncidentStatus.OPEN, IncidentSeverity.HIGH),
                alertRepository.countByStatus(AlertStatus.PENDING),
                alertRepository.countByStatus(AlertStatus.SENT),
                alertRepository.countByStatus(AlertStatus.FAILED)
        );
    }
}