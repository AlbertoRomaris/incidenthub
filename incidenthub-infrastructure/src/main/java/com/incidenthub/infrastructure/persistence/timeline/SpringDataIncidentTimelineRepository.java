package com.incidenthub.infrastructure.persistence.timeline;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataIncidentTimelineRepository extends JpaRepository<IncidentTimelineEventEntity, UUID> {

    List<IncidentTimelineEventEntity> findByIncidentIdOrderByOccurredAtAsc(UUID incidentId);
}