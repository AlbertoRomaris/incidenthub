package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.port.IncidentRepository;
import com.incidenthub.core.application.port.IncidentTimelineRepository;
import com.incidenthub.core.domain.incident.Incident;
import com.incidenthub.core.domain.incident.IncidentId;
import com.incidenthub.core.domain.timeline.IncidentTimelineEvent;
import com.incidenthub.core.domain.timeline.IncidentTimelineEventType;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ResolveIncidentUseCase {

    private final IncidentRepository incidentRepository;
    private final IncidentTimelineRepository incidentTimelineRepository;
    private final Clock clock;

    public ResolveIncidentUseCase(
            IncidentRepository incidentRepository,
            IncidentTimelineRepository incidentTimelineRepository,
            Clock clock
    ) {
        this.incidentRepository = Objects.requireNonNull(incidentRepository, "Incident repository must not be null");
        this.incidentTimelineRepository = Objects.requireNonNull(incidentTimelineRepository, "Incident timeline repository must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    public Optional<Incident> resolve(IncidentId incidentId) {
        Objects.requireNonNull(incidentId, "Incident id must not be null");

        Instant resolvedAt = Instant.now(clock);

        return incidentRepository.findById(incidentId)
                .map(incident -> incident.resolve(resolvedAt))
                .map(incidentRepository::save)
                .map(incident -> {
                    incidentTimelineRepository.save(IncidentTimelineEvent.record(
                            incident.id(),
                            IncidentTimelineEventType.INCIDENT_RESOLVED,
                            resolvedAt,
                            "Incident resolved",
                            "system",
                            Map.of(
                                    "status", incident.status().name(),
                                    "resolvedAt", resolvedAt.toString()
                            )
                    ));

                    return incident;
                });
    }
}