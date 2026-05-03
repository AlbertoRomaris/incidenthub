package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.port.IncidentRepository;
import com.incidenthub.core.domain.incident.Incident;
import com.incidenthub.core.domain.incident.IncidentId;

import java.util.Objects;
import java.util.Optional;

public class GetIncidentByIdUseCase {

    private final IncidentRepository incidentRepository;

    public GetIncidentByIdUseCase(IncidentRepository incidentRepository) {
        this.incidentRepository = Objects.requireNonNull(incidentRepository, "Incident repository must not be null");
    }

    public Optional<Incident> findById(IncidentId incidentId) {
        Objects.requireNonNull(incidentId, "Incident id must not be null");

        return incidentRepository.findById(incidentId);
    }
}