package com.incidenthub.infrastructure.persistence.evidence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataIncidentEvidenceRepository extends JpaRepository<IncidentEvidenceEntity, UUID> {

    List<IncidentEvidenceEntity> findByIncidentIdOrderByCapturedAtAsc(UUID incidentId);
}