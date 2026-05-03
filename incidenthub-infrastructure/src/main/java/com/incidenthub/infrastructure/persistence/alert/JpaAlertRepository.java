package com.incidenthub.infrastructure.persistence.alert;

import com.incidenthub.core.application.port.AlertRepository;
import com.incidenthub.core.domain.alert.Alert;
import com.incidenthub.core.domain.alert.AlertId;
import com.incidenthub.core.domain.alert.AlertStatus;
import com.incidenthub.core.domain.incident.IncidentId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaAlertRepository implements AlertRepository {

    private final SpringDataAlertRepository springDataAlertRepository;

    public JpaAlertRepository(SpringDataAlertRepository springDataAlertRepository) {
        this.springDataAlertRepository = springDataAlertRepository;
    }

    @Override
    public Alert save(Alert alert) {
        return springDataAlertRepository
                .save(AlertEntity.fromDomain(alert))
                .toDomain();
    }

    @Override
    public Optional<Alert> findById(AlertId alertId) {
        return springDataAlertRepository.findById(alertId.value())
                .map(AlertEntity::toDomain);
    }

    @Override
    public List<Alert> findByIncidentId(IncidentId incidentId) {
        return springDataAlertRepository
                .findByIncidentIdOrderByCreatedAtAsc(incidentId.value())
                .stream()
                .map(AlertEntity::toDomain)
                .toList();
    }

    @Override
    public List<Alert> findPending(int limit) {
        PageRequest pageRequest = PageRequest.of(
                0,
                limit,
                Sort.by(Sort.Direction.ASC, "createdAt")
        );

        return springDataAlertRepository.findByStatus(AlertStatus.PENDING, pageRequest)
                .stream()
                .map(AlertEntity::toDomain)
                .toList();
    }

    @Override
    public long countByStatus(AlertStatus status) {
        return springDataAlertRepository.countByStatus(status);
    }
}