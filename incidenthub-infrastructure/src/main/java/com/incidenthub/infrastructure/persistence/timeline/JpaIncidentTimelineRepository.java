package com.incidenthub.infrastructure.persistence.timeline;

import com.incidenthub.core.application.port.IncidentTimelineRepository;
import com.incidenthub.core.domain.incident.IncidentId;
import com.incidenthub.core.domain.timeline.IncidentTimelineEvent;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaIncidentTimelineRepository implements IncidentTimelineRepository {

    private final SpringDataIncidentTimelineRepository springDataIncidentTimelineRepository;

    public JpaIncidentTimelineRepository(
            SpringDataIncidentTimelineRepository springDataIncidentTimelineRepository
    ) {
        this.springDataIncidentTimelineRepository = springDataIncidentTimelineRepository;
    }

    @Override
    public IncidentTimelineEvent save(IncidentTimelineEvent event) {
        return springDataIncidentTimelineRepository
                .save(IncidentTimelineEventEntity.fromDomain(event))
                .toDomain();
    }

    @Override
    public List<IncidentTimelineEvent> findByIncidentId(IncidentId incidentId) {
        return springDataIncidentTimelineRepository
                .findByIncidentIdOrderByOccurredAtAsc(incidentId.value())
                .stream()
                .map(IncidentTimelineEventEntity::toDomain)
                .toList();
    }
}