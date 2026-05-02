package com.incidenthub.core.application.model;

import com.incidenthub.core.domain.signal.SignalId;

public record IngestSignalResult(
        SignalId signalId,
        String status
) {

    public static IngestSignalResult accepted(SignalId signalId) {
        return new IngestSignalResult(signalId, "ACCEPTED");
    }
}