package com.incidenthub.core.application.port;

import com.incidenthub.core.domain.evidence.IncidentEvidence;
import com.incidenthub.core.domain.incident.IncidentId;

import java.util.List;

public interface IncidentEvidenceRepository {

    IncidentEvidence save(IncidentEvidence evidence);

    List<IncidentEvidence> findByIncidentId(IncidentId incidentId);
}