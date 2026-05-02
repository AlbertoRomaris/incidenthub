package com.incidenthub.api.dto.signal;

public record IngestSignalResponse(
        String signalId,
        String status
) {
}