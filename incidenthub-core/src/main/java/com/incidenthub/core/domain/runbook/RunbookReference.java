package com.incidenthub.core.domain.runbook;

import com.incidenthub.core.domain.incident.IncidentType;

import java.util.Objects;
import java.util.Optional;

public record RunbookReference(
        IncidentType incidentType,
        String title,
        String documentPath,
        String summary
) {

    public RunbookReference {
        Objects.requireNonNull(incidentType, "Incident type must not be null");
        title = requireText(title, "Runbook title must not be blank");
        documentPath = requireText(documentPath, "Runbook document path must not be blank");
        summary = summary == null ? "" : summary.trim();
    }

    public static Optional<RunbookReference> forIncidentType(IncidentType incidentType) {
        if (incidentType == null) {
            return Optional.empty();
        }

        return switch (incidentType) {
            case HIGH_ERROR_RATE -> Optional.of(new RunbookReference(
                    incidentType,
                    "High Error Rate Runbook",
                    "docs/runbooks/high-error-rate.md",
                    "Investigate repeated error signals emitted by a service."
            ));
            case HIGH_LATENCY -> Optional.of(new RunbookReference(
                    incidentType,
                    "High Latency Runbook",
                    "docs/runbooks/high-latency.md",
                    "Investigate latency degradation and slow downstream responses."
            ));
            case TIMEOUT_SPIKE -> Optional.of(new RunbookReference(
                    incidentType,
                    "Timeout Spike Runbook",
                    "docs/runbooks/dependency-failure.md",
                    "Investigate repeated timeout signals and dependency degradation."
            ));
            case SERVICE_DOWN -> Optional.of(new RunbookReference(
                    incidentType,
                    "Service Down Runbook",
                    "docs/runbooks/service-down.md",
                    "Investigate service unavailability and failed health checks."
            ));
            case DEPENDENCY_FAILURE -> Optional.of(new RunbookReference(
                    incidentType,
                    "Dependency Failure Runbook",
                    "docs/runbooks/dependency-failure.md",
                    "Investigate failures caused by downstream systems."
            ));
            case QUEUE_BACKLOG -> Optional.of(new RunbookReference(
                    incidentType,
                    "Queue Backlog Runbook",
                    "docs/runbooks/queue-backlog.md",
                    "Investigate growing backlog and worker processing delays."
            ));
            case CUSTOM -> Optional.empty();
        };
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }
}