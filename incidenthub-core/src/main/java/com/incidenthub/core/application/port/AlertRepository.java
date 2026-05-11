package com.incidenthub.core.application.port;

import com.incidenthub.core.domain.alert.Alert;
import com.incidenthub.core.domain.alert.AlertId;
import com.incidenthub.core.domain.alert.AlertStatus;
import com.incidenthub.core.domain.incident.IncidentId;

import java.util.List;
import java.util.Optional;

public interface AlertRepository {

    Alert save(Alert alert);

    Optional<Alert> findById(AlertId alertId);

    List<Alert> findByIncidentId(IncidentId incidentId);

    List<Alert> findPending(int limit);

    long countByStatus(AlertStatus status);

    List<Alert> findRecent(int limit);
}