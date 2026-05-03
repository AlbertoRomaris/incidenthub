package com.incidenthub.core.application.usecase;

import com.incidenthub.core.application.model.SendPendingAlertsResult;
import com.incidenthub.core.application.port.AlertRepository;
import com.incidenthub.core.application.port.AlertSender;
import com.incidenthub.core.application.port.IncidentTimelineRepository;
import com.incidenthub.core.domain.alert.Alert;
import com.incidenthub.core.domain.timeline.IncidentTimelineEvent;
import com.incidenthub.core.domain.timeline.IncidentTimelineEventType;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SendPendingAlertsUseCase {

    private final AlertRepository alertRepository;
    private final AlertSender alertSender;
    private final IncidentTimelineRepository incidentTimelineRepository;
    private final Clock clock;

    public SendPendingAlertsUseCase(
            AlertRepository alertRepository,
            AlertSender alertSender,
            IncidentTimelineRepository incidentTimelineRepository,
            Clock clock
    ) {
        this.alertRepository = Objects.requireNonNull(alertRepository, "Alert repository must not be null");
        this.alertSender = Objects.requireNonNull(alertSender, "Alert sender must not be null");
        this.incidentTimelineRepository = Objects.requireNonNull(
                incidentTimelineRepository,
                "Incident timeline repository must not be null"
        );
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    public SendPendingAlertsResult sendPending(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Alert processing limit must be greater than zero");
        }

        List<Alert> pendingAlerts = alertRepository.findPending(limit);

        int sentAlerts = 0;
        int failedAlerts = 0;

        for (Alert alert : pendingAlerts) {
            try {
                alertSender.send(alert);

                Instant sentAt = Instant.now(clock);
                Alert sentAlert = alertRepository.save(alert.markSent(sentAt));

                recordAlertSent(sentAlert, sentAt);
                sentAlerts++;
            } catch (Exception exception) {
                Instant failedAt = Instant.now(clock);
                Alert failedAlert = alertRepository.save(alert.markFailed(failedAt, exception.getMessage()));

                recordAlertFailed(failedAlert, failedAt, exception.getMessage());
                failedAlerts++;
            }
        }

        return SendPendingAlertsResult.of(
                pendingAlerts.size(),
                sentAlerts,
                failedAlerts
        );
    }

    private void recordAlertSent(Alert alert, Instant occurredAt) {
        incidentTimelineRepository.save(IncidentTimelineEvent.record(
                alert.incidentId(),
                IncidentTimelineEventType.ALERT_SENT,
                occurredAt,
                "Alert sent through channel: " + alert.channel().name(),
                "worker",
                Map.of(
                        "alertId", alert.id().value().toString(),
                        "channel", alert.channel().name(),
                        "status", alert.status().name()
                )
        ));
    }

    private void recordAlertFailed(
            Alert alert,
            Instant occurredAt,
            String failureReason
    ) {
        incidentTimelineRepository.save(IncidentTimelineEvent.record(
                alert.incidentId(),
                IncidentTimelineEventType.ALERT_FAILED,
                occurredAt,
                "Alert failed through channel: " + alert.channel().name(),
                "worker",
                Map.of(
                        "alertId", alert.id().value().toString(),
                        "channel", alert.channel().name(),
                        "status", alert.status().name(),
                        "failureReason", failureReason == null ? "" : failureReason
                )
        ));
    }
}