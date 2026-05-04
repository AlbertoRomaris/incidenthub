package com.incidenthub.core.application.port;

import com.incidenthub.core.application.model.SignalProcessingTask;
import com.incidenthub.core.application.model.SignalProcessingTaskStatus;
import com.incidenthub.core.domain.signal.SignalId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SignalProcessingTaskRepository {

    SignalProcessingTask createPending(SignalId signalId, Instant createdAt);

    List<SignalProcessingTask> claimPending(String workerId, int limit, Instant lockedAt);

    void markProcessed(SignalId signalId, Instant processedAt);

    void markFailed(SignalId signalId, String errorMessage, Instant failedAt);

    long countByStatus(SignalProcessingTaskStatus status);

    Optional<Instant> findOldestPendingTaskCreatedAt();

    Optional<Double> findAverageProcessedTaskLatencyMsSince(Instant since);

}