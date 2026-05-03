package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.port.IncidentEvidenceRepository;
import com.incidenthub.core.domain.evidence.IncidentEvidence;
import com.incidenthub.core.domain.incident.IncidentId;

import java.util.List;
import java.util.Objects;

public class ListIncidentEvidenceUseCase {

    private final IncidentEvidenceRepository incidentEvidenceRepository;

    public ListIncidentEvidenceUseCase(IncidentEvidenceRepository incidentEvidenceRepository) {
        this.incidentEvidenceRepository = Objects.requireNonNull(
                incidentEvidenceRepository,
                "Incident evidence repository must not be null"
        );
    }

    public List<IncidentEvidence> findByIncidentId(IncidentId incidentId) {
        Objects.requireNonNull(incidentId, "Incident id must not be null");

        return incidentEvidenceRepository.findByIncidentId(incidentId);
    }
}