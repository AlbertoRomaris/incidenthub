package com.incidenthub.core.application.model;

import com.incidenthub.core.domain.incident.IncidentId;
import com.incidenthub.core.domain.signal.SignalId;

import java.util.List;
import java.util.Objects;

public record ProcessSignalResult(
        SignalId signalId,
        int evaluatedRules,
        int matchedRules,
        List<IncidentId> affectedIncidents
) {

    public ProcessSignalResult {
        Objects.requireNonNull(signalId, "Signal id must not be null");
        affectedIncidents = affectedIncidents == null ? List.of() : List.copyOf(affectedIncidents);

        if (evaluatedRules < 0) {
            throw new IllegalArgumentException("Evaluated rules count must not be negative");
        }

        if (matchedRules < 0) {
            throw new IllegalArgumentException("Matched rules count must not be negative");
        }

        if (matchedRules > evaluatedRules) {
            throw new IllegalArgumentException("Matched rules count must not exceed evaluated rules count");
        }
    }

    public static ProcessSignalResult of(
            SignalId signalId,
            int evaluatedRules,
            int matchedRules,
            List<IncidentId> affectedIncidents
    ) {
        return new ProcessSignalResult(
                signalId,
                evaluatedRules,
                matchedRules,
                affectedIncidents
        );
    }
}