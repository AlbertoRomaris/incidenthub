package com.incidenthub.api.dto.incident;

import java.time.Instant;
import java.util.Map;

public record IncidentEvidenceResponse(
        String evidenceId,
        String incidentId,
        String signalId,
        String ruleId,
        Instant capturedAt,
        String summary,
        Map<String, Object> attributes
) {
}