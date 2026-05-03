package com.incidenthub.core.application.port;

import com.incidenthub.core.domain.incident.DeduplicationKey;
import com.incidenthub.core.domain.incident.Incident;
import com.incidenthub.core.domain.incident.IncidentId;
import com.incidenthub.core.domain.incident.IncidentStatus;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository {

    Incident save(Incident incident);

    Optional<Incident> findById(IncidentId incidentId);

    Optional<Incident> findActiveByDeduplicationKey(DeduplicationKey deduplicationKey);

    List<Incident> findOpenIncidents(int limit);

    List<Incident> findByStatus(IncidentStatus status, int limit);
}