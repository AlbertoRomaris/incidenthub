package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.port.IncidentRepository;
import com.incidenthub.core.domain.incident.Incident;
import com.incidenthub.core.domain.incident.IncidentId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class ResolveIncidentUseCase {

    private final IncidentRepository incidentRepository;
    private final Clock clock;

    public ResolveIncidentUseCase(
            IncidentRepository incidentRepository,
            Clock clock
    ) {
        this.incidentRepository = Objects.requireNonNull(incidentRepository, "Incident repository must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    public Optional<Incident> resolve(IncidentId incidentId) {
        Objects.requireNonNull(incidentId, "Incident id must not be null");

        return incidentRepository.findById(incidentId)
                .map(incident -> incident.resolve(Instant.now(clock)))
                .map(incidentRepository::save);
    }
}