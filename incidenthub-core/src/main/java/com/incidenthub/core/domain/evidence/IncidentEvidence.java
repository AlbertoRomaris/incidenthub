package com.incidenthub.core.domain.evidence;

import com.incidenthub.core.domain.incident.IncidentId;
import com.incidenthub.core.domain.rule.RuleId;
import com.incidenthub.core.domain.signal.SignalId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record IncidentEvidence(
        IncidentEvidenceId id,
        IncidentId incidentId,
        SignalId signalId,
        RuleId ruleId,
        Instant capturedAt,
        String summary,
        Map<String, Object> attributes
) {

    public IncidentEvidence {
        Objects.requireNonNull(id, "Incident evidence id must not be null");
        Objects.requireNonNull(incidentId, "Incident id must not be null");
        Objects.requireNonNull(signalId, "Signal id must not be null");
        Objects.requireNonNull(ruleId, "Rule id must not be null");
        Objects.requireNonNull(capturedAt, "Captured timestamp must not be null");
        summary = requireText(summary, "Evidence summary must not be blank");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static IncidentEvidence captured(
            IncidentId incidentId,
            SignalId signalId,
            RuleId ruleId,
            Instant capturedAt,
            String summary,
            Map<String, Object> attributes
    ) {
        return new IncidentEvidence(
                IncidentEvidenceId.newId(),
                incidentId,
                signalId,
                ruleId,
                capturedAt,
                summary,
                attributes
        );
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}