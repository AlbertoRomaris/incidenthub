package com.incidenthub.infrastructure.persistence.evidence;

import com.incidenthub.core.application.port.IncidentEvidenceRepository;
import com.incidenthub.core.domain.evidence.IncidentEvidence;
import com.incidenthub.core.domain.incident.IncidentId;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaIncidentEvidenceRepository implements IncidentEvidenceRepository {

    private final SpringDataIncidentEvidenceRepository springDataIncidentEvidenceRepository;

    public JpaIncidentEvidenceRepository(SpringDataIncidentEvidenceRepository springDataIncidentEvidenceRepository) {
        this.springDataIncidentEvidenceRepository = springDataIncidentEvidenceRepository;
    }

    @Override
    public IncidentEvidence save(IncidentEvidence evidence) {
        return springDataIncidentEvidenceRepository
                .save(IncidentEvidenceEntity.fromDomain(evidence))
                .toDomain();
    }

    @Override
    public List<IncidentEvidence> findByIncidentId(IncidentId incidentId) {
        return springDataIncidentEvidenceRepository
                .findByIncidentIdOrderByCapturedAtAsc(incidentId.value())
                .stream()
                .map(IncidentEvidenceEntity::toDomain)
                .toList();
    }
}