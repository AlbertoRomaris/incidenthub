package com.incidenthub.api.dto.incident;

import com.incidenthub.core.domain.alert.Alert;
import com.incidenthub.core.domain.alert.AlertChannel;
import com.incidenthub.core.domain.alert.AlertStatus;

import java.time.Instant;
import java.util.Map;

public record IncidentAlertResponse(
        String alertId,
        String incidentId,
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

    public static IncidentAlertResponse from(Alert alert) {
        return new IncidentAlertResponse(
                alert.id().value().toString(),
                alert.incidentId().value().toString(),
                alert.channel(),
                alert.status(),
                alert.title(),
                alert.message(),
                alert.createdAt(),
                alert.sentAt(),
                alert.failedAt(),
                alert.failureReason(),
                alert.attributes()
        );
    }
}