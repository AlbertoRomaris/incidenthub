package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.port.IncidentRepository;
import com.incidenthub.core.domain.incident.Incident;

import java.util.List;
import java.util.Objects;

public class ListOpenIncidentsUseCase {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    private final IncidentRepository incidentRepository;

    public ListOpenIncidentsUseCase(IncidentRepository incidentRepository) {
        this.incidentRepository = Objects.requireNonNull(incidentRepository, "Incident repository must not be null");
    }

    public List<Incident> listOpen(Integer requestedLimit) {
        int limit = normalizeLimit(requestedLimit);

        return incidentRepository.findOpenIncidents(limit);
    }

    private int normalizeLimit(Integer requestedLimit) {
        if (requestedLimit == null || requestedLimit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(requestedLimit, MAX_LIMIT);
    }
}