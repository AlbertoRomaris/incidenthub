package com.incidenthub.core.domain.signal;

public enum SignalType {
    ERROR,
    LATENCY,
    TIMEOUT,
    HEARTBEAT,
    DEPENDENCY_FAILURE,
    QUEUE_BACKLOG,
    DEPLOYMENT_EVENT,
    CUSTOM
}