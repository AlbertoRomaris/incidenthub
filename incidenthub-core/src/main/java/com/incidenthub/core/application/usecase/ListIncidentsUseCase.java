package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.port.IncidentRepository;
import com.incidenthub.core.domain.incident.Incident;
import com.incidenthub.core.domain.incident.IncidentStatus;

import java.util.List;
import java.util.Objects;

public class ListIncidentsUseCase {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    private final IncidentRepository incidentRepository;

    public ListIncidentsUseCase(IncidentRepository incidentRepository) {
        this.incidentRepository = Objects.requireNonNull(incidentRepository, "Incident repository must not be null");
    }

    public List<Incident> list(IncidentStatus status, Integer requestedLimit) {
        int limit = normalizeLimit(requestedLimit);

        if (status == null) {
            return incidentRepository.findOpenIncidents(limit);
        }

        return incidentRepository.findByStatus(status, limit);
    }

    private int normalizeLimit(Integer requestedLimit) {
        if (requestedLimit == null || requestedLimit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(requestedLimit, MAX_LIMIT);
    }
}