package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.port.IncidentTimelineRepository;
import com.incidenthub.core.domain.incident.IncidentId;
import com.incidenthub.core.domain.timeline.IncidentTimelineEvent;

import java.util.List;
import java.util.Objects;

public class ListIncidentTimelineUseCase {

    private final IncidentTimelineRepository incidentTimelineRepository;

    public ListIncidentTimelineUseCase(IncidentTimelineRepository incidentTimelineRepository) {
        this.incidentTimelineRepository = Objects.requireNonNull(
                incidentTimelineRepository,
                "Incident timeline repository must not be null"
        );
    }

    public List<IncidentTimelineEvent> findByIncidentId(IncidentId incidentId) {
        Objects.requireNonNull(incidentId, "Incident id must not be null");

        return incidentTimelineRepository.findByIncidentId(incidentId);
    }
}