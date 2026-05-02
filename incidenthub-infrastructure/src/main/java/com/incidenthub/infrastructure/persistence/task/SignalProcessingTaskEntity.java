package com.incidenthub.infrastructure.persistence.task;

import com.incidenthub.core.application.model.SignalProcessingTask;
import com.incidenthub.core.application.model.SignalProcessingTaskStatus;
import com.incidenthub.core.domain.signal.SignalId;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "signal_processing_tasks")
public class SignalProcessingTaskEntity {

    @Id
    @Column(name = "signal_id")
    private UUID signalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private SignalProcessingTaskStatus status;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "locked_by", length = 120)
    private String lockedBy;

    @Column(name = "locked_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant lockedAt;

    @Column(name = "processed_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant processedAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant updatedAt;

    protected SignalProcessingTaskEntity() {
        // Required by JPA
    }

    private SignalProcessingTaskEntity(
            UUID signalId,
            SignalProcessingTaskStatus status,
            int attempts,
            String lockedBy,
            Instant lockedAt,
            Instant processedAt,
            String lastError,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.signalId = signalId;
        this.status = status;
        this.attempts = attempts;
        this.lockedBy = lockedBy;
        this.lockedAt = lockedAt;
        this.processedAt = processedAt;
        this.lastError = lastError;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SignalProcessingTaskEntity pending(SignalId signalId, Instant createdAt) {
        return new SignalProcessingTaskEntity(
                signalId.value(),
                SignalProcessingTaskStatus.PENDING,
                0,
                null,
                null,
                null,
                null,
                createdAt,
                createdAt
        );
    }

    public SignalProcessingTask toDomain() {
        return new SignalProcessingTask(
                SignalId.from(signalId),
                status,
                attempts,
                lockedBy,
                lockedAt,
                processedAt,
                lastError,
                createdAt,
                updatedAt
        );
    }

    public void claim(String workerId, Instant lockedAt) {
        this.status = SignalProcessingTaskStatus.PROCESSING;
        this.attempts = this.attempts + 1;
        this.lockedBy = workerId;
        this.lockedAt = lockedAt;
        this.updatedAt = lockedAt;
    }

    public void markProcessed(Instant processedAt) {
        this.status = SignalProcessingTaskStatus.PROCESSED;
        this.processedAt = processedAt;
        this.lockedBy = null;
        this.lockedAt = null;
        this.lastError = null;
        this.updatedAt = processedAt;
    }

    public void markFailed(String errorMessage, Instant failedAt) {
        this.status = SignalProcessingTaskStatus.FAILED;
        this.lastError = errorMessage;
        this.lockedBy = null;
        this.lockedAt = null;
        this.updatedAt = failedAt;
    }
}