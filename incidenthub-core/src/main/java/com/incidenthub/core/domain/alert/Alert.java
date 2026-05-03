package com.incidenthub.core.domain.alert;

import com.incidenthub.core.domain.incident.IncidentId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record Alert(
        AlertId id,
        IncidentId incidentId,
        AlertChannel channel,
        AlertStatus status,
        String title,
        String message,
        Instant createdAt,
        Instant sentAt,
        Instant failedAt,
        String failureReason,
        Map<String, Object> attributes
) {

    public Alert {
        Objects.requireNonNull(id, "Alert id must not be null");
        Objects.requireNonNull(incidentId, "Incident id must not be null");
        Objects.requireNonNull(channel, "Alert channel must not be null");
        Objects.requireNonNull(status, "Alert status must not be null");
        title = requireText(title, "Alert title must not be blank");
        message = requireText(message, "Alert message must not be blank");
        Objects.requireNonNull(createdAt, "Alert created timestamp must not be null");
        failureReason = failureReason == null ? null : failureReason.trim();
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static Alert pending(
            IncidentId incidentId,
            AlertChannel channel,
            String title,
            String message,
            Instant createdAt,
            Map<String, Object> attributes
    ) {
        return new Alert(
                AlertId.newId(),
                incidentId,
                channel,
                AlertStatus.PENDING,
                title,
                message,
                createdAt,
                null,
                null,
                null,
                attributes
        );
    }

    public Alert markSent(Instant sentAt) {
        Objects.requireNonNull(sentAt, "Sent timestamp must not be null");

        if (status != AlertStatus.PENDING) {
            throw new IllegalStateException("Only pending alerts can be marked as sent");
        }

        return new Alert(
                id,
                incidentId,
                channel,
                AlertStatus.SENT,
                title,
                message,
                createdAt,
                sentAt,
                failedAt,
                null,
                attributes
        );
    }

    public Alert markFailed(Instant failedAt, String failureReason) {
        Objects.requireNonNull(failedAt, "Failed timestamp must not be null");

        if (status != AlertStatus.PENDING) {
            throw new IllegalStateException("Only pending alerts can be marked as failed");
        }

        String normalizedFailureReason = requireText(failureReason, "Failure reason must not be blank");

        return new Alert(
                id,
                incidentId,
                channel,
                AlertStatus.FAILED,
                title,
                message,
                createdAt,
                sentAt,
                failedAt,
                normalizedFailureReason,
                attributes
        );
    }

    public boolean isPending() {
        return status == AlertStatus.PENDING;
    }

    public boolean isFinished() {
        return status == AlertStatus.SENT
                || status == AlertStatus.FAILED
                || status == AlertStatus.SUPPRESSED;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}