package com.incidenthub.infrastructure.persistence.incident;

import com.incidenthub.core.domain.incident.IncidentSeverity;
import com.incidenthub.core.domain.incident.IncidentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataIncidentRepository extends JpaRepository<IncidentEntity, UUID> {

    Optional<IncidentEntity> findFirstByDeduplicationKeyAndStatusIn(
            String deduplicationKey,
            Collection<IncidentStatus> statuses
    );

    List<IncidentEntity> findByStatusIn(
            Collection<IncidentStatus> statuses,
            Pageable pageable
    );

    List<IncidentEntity> findByStatus(
            IncidentStatus status,
            Pageable pageable
    );

    long countByStatus(IncidentStatus status);

    long countByStatusAndSeverity(IncidentStatus status, IncidentSeverity severity);
}