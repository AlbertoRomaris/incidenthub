package com.incidenthub.core.domain.timeline;

public enum IncidentTimelineEventType {
    INCIDENT_OPENED,
    INCIDENT_ACKNOWLEDGED,
    INCIDENT_RESOLVED,
    INCIDENT_REOPENED,
    EVIDENCE_ATTACHED,
    RUNBOOK_ATTACHED,
    ALERT_REQUESTED,
    ALERT_SENT,
    ALERT_FAILED
}