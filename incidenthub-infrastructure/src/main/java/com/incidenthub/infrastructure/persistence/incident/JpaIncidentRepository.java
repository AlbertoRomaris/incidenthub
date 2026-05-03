package com.incidenthub.infrastructure.persistence.incident;

import com.incidenthub.core.application.port.IncidentRepository;
import com.incidenthub.core.domain.incident.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaIncidentRepository implements IncidentRepository {

    private static final List<IncidentStatus> ACTIVE_STATUSES = List.of(
            IncidentStatus.OPEN,
            IncidentStatus.ACKNOWLEDGED
    );

    private final SpringDataIncidentRepository springDataIncidentRepository;

    public JpaIncidentRepository(SpringDataIncidentRepository springDataIncidentRepository) {
        this.springDataIncidentRepository = springDataIncidentRepository;
    }

    @Override
    public Incident save(Incident incident) {
        IncidentEntity entity = IncidentEntity.fromDomain(incident);

        springDataIncidentRepository.findById(incident.id().value())
                .ifPresent(entity::preserveCreatedAtFrom);

        return springDataIncidentRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Incident> findById(IncidentId incidentId) {
        return springDataIncidentRepository.findById(incidentId.value())
                .map(IncidentEntity::toDomain);
    }

    @Override
    public Optional<Incident> findActiveByDeduplicationKey(DeduplicationKey deduplicationKey) {
        return springDataIncidentRepository
                .findFirstByDeduplicationKeyAndStatusIn(deduplicationKey.value(), ACTIVE_STATUSES)
                .map(IncidentEntity::toDomain);
    }

    @Override
    public List<Incident> findOpenIncidents(int limit) {
        PageRequest pageRequest = PageRequest.of(
                0,
                limit,
                Sort.by(Sort.Direction.DESC, "openedAt")
        );

        return springDataIncidentRepository.findByStatusIn(ACTIVE_STATUSES, pageRequest)
                .stream()
                .map(IncidentEntity::toDomain)
                .toList();
    }

    @Override
    public List<Incident> findByStatus(IncidentStatus status, int limit) {
        PageRequest pageRequest = PageRequest.of(
                0,
                limit,
                Sort.by(Sort.Direction.DESC, "openedAt")
        );

        return springDataIncidentRepository.findByStatus(status, pageRequest)
                .stream()
                .map(IncidentEntity::toDomain)
                .toList();
    }

    @Override
    public long countByStatus(IncidentStatus status) {
        return springDataIncidentRepository.countByStatus(status);
    }

    @Override
    public long countByStatusAndSeverity(IncidentStatus status, IncidentSeverity severity) {
        return springDataIncidentRepository.countByStatusAndSeverity(status, severity);
    }
}