package com.incidenthub.core.application.model;

public record SendPendingAlertsResult(
        int processedAlerts,
        int sentAlerts,
        int failedAlerts
) {

    public SendPendingAlertsResult {
        if (processedAlerts < 0) {
            throw new IllegalArgumentException("Processed alerts count must not be negative");
        }

        if (sentAlerts < 0) {
            throw new IllegalArgumentException("Sent alerts count must not be negative");
        }

        if (failedAlerts < 0) {
            throw new IllegalArgumentException("Failed alerts count must not be negative");
        }

        if (sentAlerts + failedAlerts > processedAlerts) {
            throw new IllegalArgumentException("Sent and failed alerts must not exceed processed alerts");
        }
    }

    public static SendPendingAlertsResult of(
            int processedAlerts,
            int sentAlerts,
            int failedAlerts
    ) {
        return new SendPendingAlertsResult(
                processedAlerts,
                sentAlerts,
                failedAlerts
        );
    }
}