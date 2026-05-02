package com.incidenthub.core.application.model;

import com.incidenthub.core.domain.signal.SignalId;

import java.time.Instant;
import java.util.Objects;

public record SignalProcessingTask(
        SignalId signalId,
        SignalProcessingTaskStatus status,
        int attempts,
        String lockedBy,
        Instant lockedAt,
        Instant processedAt,
        String lastError,
        Instant createdAt,
        Instant updatedAt
) {

    public SignalProcessingTask {
        Objects.requireNonNull(signalId, "Signal id must not be null");
        Objects.requireNonNull(status, "Processing task status must not be null");
        Objects.requireNonNull(createdAt, "Created timestamp must not be null");
        Objects.requireNonNull(updatedAt, "Updated timestamp must not be null");

        if (attempts < 0) {
            throw new IllegalArgumentException("Attempts must not be negative");
        }

        lockedBy = lockedBy == null ? null : lockedBy.trim();
        lastError = lastError == null ? null : lastError.trim();
    }

    public boolean isPending() {
        return status == SignalProcessingTaskStatus.PENDING;
    }

    public boolean isProcessing() {
        return status == SignalProcessingTaskStatus.PROCESSING;
    }

    public boolean isFinished() {
        return status == SignalProcessingTaskStatus.PROCESSED
                || status == SignalProcessingTaskStatus.FAILED;
    }
}