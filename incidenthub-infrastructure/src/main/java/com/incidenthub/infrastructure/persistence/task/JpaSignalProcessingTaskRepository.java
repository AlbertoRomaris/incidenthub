package com.incidenthub.infrastructure.persistence.task;

import com.incidenthub.core.application.model.SignalProcessingTask;
import com.incidenthub.core.application.model.SignalProcessingTaskStatus;
import com.incidenthub.core.application.port.SignalProcessingTaskRepository;
import com.incidenthub.core.domain.signal.SignalId;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaSignalProcessingTaskRepository implements SignalProcessingTaskRepository {

    private final SpringDataSignalProcessingTaskRepository springDataSignalProcessingTaskRepository;

    public JpaSignalProcessingTaskRepository(
            SpringDataSignalProcessingTaskRepository springDataSignalProcessingTaskRepository
    ) {
        this.springDataSignalProcessingTaskRepository = springDataSignalProcessingTaskRepository;
    }

    @Override
    public SignalProcessingTask createPending(SignalId signalId, Instant createdAt) {
        return springDataSignalProcessingTaskRepository.findById(signalId.value())
                .map(SignalProcessingTaskEntity::toDomain)
                .orElseGet(() -> springDataSignalProcessingTaskRepository
                        .save(SignalProcessingTaskEntity.pending(signalId, createdAt))
                        .toDomain());
    }

    @Override
    @Transactional
    public List<SignalProcessingTask> claimPending(String workerId, int limit, Instant lockedAt) {
        List<SignalProcessingTaskEntity> claimedEntities =
                springDataSignalProcessingTaskRepository.findPendingForUpdate(limit);

        claimedEntities.forEach(entity -> entity.claim(workerId, lockedAt));

        return claimedEntities.stream()
                .map(SignalProcessingTaskEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void markProcessed(SignalId signalId, Instant processedAt) {
        SignalProcessingTaskEntity entity = springDataSignalProcessingTaskRepository
                .findById(signalId.value())
                .orElseThrow(() -> new IllegalArgumentException("Signal processing task not found"));

        entity.markProcessed(processedAt);
    }

    @Override
    @Transactional
    public void markFailed(SignalId signalId, String errorMessage, Instant failedAt) {
        SignalProcessingTaskEntity entity = springDataSignalProcessingTaskRepository
                .findById(signalId.value())
                .orElseThrow(() -> new IllegalArgumentException("Signal processing task not found"));

        entity.markFailed(errorMessage, failedAt);
    }

    @Override
    public long countByStatus(SignalProcessingTaskStatus status) {
        return springDataSignalProcessingTaskRepository.countByStatus(status);
    }

    @Override
    public Optional<Instant> findOldestPendingTaskCreatedAt() {
        return springDataSignalProcessingTaskRepository
                .findCreatedAtByStatusOrderByCreatedAtAsc(
                        SignalProcessingTaskStatus.PENDING,
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Double> findAverageProcessedTaskLatencyMsSince(Instant since) {
        return Optional.ofNullable(
                springDataSignalProcessingTaskRepository.findAverageProcessedTaskLatencyMsSince(since)
        );
    }
}