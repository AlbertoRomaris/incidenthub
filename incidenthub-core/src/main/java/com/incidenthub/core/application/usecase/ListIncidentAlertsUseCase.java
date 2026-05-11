package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.port.AlertRepository;
import com.incidenthub.core.domain.alert.Alert;
import com.incidenthub.core.domain.incident.IncidentId;

import java.util.List;
import java.util.Objects;

public class ListIncidentAlertsUseCase {

    private final AlertRepository alertRepository;

    public ListIncidentAlertsUseCase(AlertRepository alertRepository) {
        this.alertRepository = Objects.requireNonNull(alertRepository, "Alert repository must not be null");
    }

    public List<Alert> findByIncidentId(IncidentId incidentId) {
        Objects.requireNonNull(incidentId, "Incident id must not be null");

        return alertRepository.findByIncidentId(incidentId);
    }

    public List<Alert> findRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return alertRepository.findRecent(safeLimit);
    }
}