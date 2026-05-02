package com.incidenthub.core.domain.incident;

public enum IncidentType {
    HIGH_ERROR_RATE,
    HIGH_LATENCY,
    TIMEOUT_SPIKE,
    SERVICE_DOWN,
    DEPENDENCY_FAILURE,
    QUEUE_BACKLOG,
    CUSTOM
}