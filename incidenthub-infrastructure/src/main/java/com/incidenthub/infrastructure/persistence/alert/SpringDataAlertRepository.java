package com.incidenthub.infrastructure.persistence.alert;

import com.incidenthub.core.domain.alert.AlertStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataAlertRepository extends JpaRepository<AlertEntity, UUID> {

    List<AlertEntity> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);

    List<AlertEntity> findByStatus(AlertStatus status, Pageable pageable);
}