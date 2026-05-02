package com.incidenthub.core.application.port;

import com.incidenthub.core.application.model.SignalProcessingTask;
import com.incidenthub.core.domain.signal.SignalId;

import java.time.Instant;
import java.util.List;

public interface SignalProcessingTaskRepository {

    SignalProcessingTask createPending(SignalId signalId, Instant createdAt);

    List<SignalProcessingTask> claimPending(String workerId, int limit, Instant lockedAt);

    void markProcessed(SignalId signalId, Instant processedAt);

    void markFailed(SignalId signalId, String errorMessage, Instant failedAt);
}