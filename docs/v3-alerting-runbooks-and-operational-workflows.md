# V3 – Alerting, Runbooks & Operational Workflows

## Overview

IncidentHub V3 evolves the platform from incident detection into operational response.

V2 introduced asynchronous rule evaluation, deduplicated incidents, evidence and lifecycle state.

V3 adds the operational layer around incidents:

- incident timeline
- runbook association
- alert request creation
- alert delivery state
- log-based alert sender
- incident alert query endpoint
- operational dashboard summary
- initial incident runbooks

The goal of V3 is to make incidents not only detectable, but actionable and explainable.

---

## Why V3 Exists

V2 could answer:

```text
Which incident was created?
Which rule triggered it?
Which signals were attached as evidence?
Was the incident acknowledged or resolved?
```

V3 starts answering operational questions:

```text
What happened during the incident?
Which runbook should be followed?
Was an alert sent?
Which channel was used?
What is the current operational state of the platform?
```

This moves IncidentHub from:

```text
incident detection
```

to:

```text
incident response workflow
```

---

## High-Level Flow

```text
Demo Payment Service
        ↓
IncidentHub API
        ↓
Signal Processing Task
        ↓
IncidentHub Worker
        ↓
Rule Evaluation
        ↓
Incident Created / Updated
        ↓
Timeline Event Recorded
        ↓
Runbook Attached
        ↓
Alert Requested
        ↓
Alert Sent
        ↓
Dashboard Updated
```

---

## Main V3 Capabilities

### Incident Timeline

Each important incident action is recorded as a timeline event.

Current event types include:

```text
INCIDENT_OPENED
RUNBOOK_ATTACHED
EVIDENCE_ATTACHED
ALERT_REQUESTED
ALERT_SENT
ALERT_FAILED
INCIDENT_ACKNOWLEDGED
INCIDENT_RESOLVED
```

The timeline makes the incident auditable and easier to explain.

Example endpoint:

```http
GET /incidents/{incidentId}/timeline
```

Example events:

```text
INCIDENT_OPENED
RUNBOOK_ATTACHED
ALERT_REQUESTED
EVIDENCE_ATTACHED
ALERT_SENT
INCIDENT_RESOLVED
```

---

## Runbook Association

When a new incident is opened, IncidentHub associates a runbook based on the incident type.

Examples:

```text
HIGH_LATENCY     → docs/runbooks/high-latency.md
HIGH_ERROR_RATE  → docs/runbooks/high-error-rate.md
TIMEOUT_SPIKE    → docs/runbooks/dependency-failure.md
SERVICE_DOWN     → docs/runbooks/service-down.md
QUEUE_BACKLOG    → docs/runbooks/queue-backlog.md
```

The runbook is recorded in the incident timeline as:

```text
RUNBOOK_ATTACHED
```

This allows operators to move directly from detection to response.

---

## Alerting Model

V3 introduces an alert domain model.

An alert has:

- alert id
- incident id
- channel
- status
- title
- message
- created timestamp
- sent timestamp
- failed timestamp
- failure reason
- attributes

Current alert statuses:

```text
PENDING
SENT
FAILED
SUPPRESSED
```

Current alert channels:

```text
LOG
WEBHOOK
EMAIL
SLACK
SNS
```

V3 implements the `LOG` channel.

Other channels are intentionally modeled but not implemented yet.

---

## Alert Flow

When a new incident is opened:

```text
Incident opened
        ↓
Alert created with status PENDING
        ↓
Timeline records ALERT_REQUESTED
        ↓
Worker processes pending alert
        ↓
LogAlertSender sends alert to logs
        ↓
Alert status becomes SENT
        ↓
Timeline records ALERT_SENT
```

This gives IncidentHub an extensible alerting architecture without introducing external infrastructure yet.

---

## Log Alert Sender

The first alert sender is intentionally simple.

It writes alerts to worker logs:

```text
INCIDENT ALERT | alertId=... | incidentId=... | title=...
```

This validates the alerting abstraction while keeping the system local and deterministic.

Later versions can add:

- Slack sender
- webhook sender
- email sender
- AWS SNS sender

without changing the incident detection core.

---

## Alert Query Endpoint

V3 exposes incident alerts through the API:

```http
GET /incidents/{incidentId}/alerts
```

Example response fields:

```text
alertId
incidentId
channel
status
title
message
createdAt
sentAt
failedAt
failureReason
attributes
```

This allows the API to show whether an incident was actually notified.

---

## Operational Dashboard Summary

V3 adds a dashboard summary endpoint:

```http
GET /dashboard/summary
```

It returns high-level operational counters:

```text
activeIncidents
openIncidents
acknowledgedIncidents
resolvedIncidents
criticalOpenIncidents
highOpenIncidents
pendingAlerts
sentAlerts
failedAlerts
```

Example:

```text
activeIncidents       : 2
openIncidents         : 2
acknowledgedIncidents : 0
resolvedIncidents     : 4
criticalOpenIncidents : 0
highOpenIncidents     : 1
pendingAlerts         : 0
sentAlerts            : 3
failedAlerts          : 0
```

This gives IncidentHub a concise operational overview.

---

## Validated Incident Types

V3 was validated end-to-end with multiple incident types.

### High Latency

```text
3 LATENCY signals within 60 seconds
        ↓
HIGH_LATENCY incident
        ↓
High Latency Runbook
        ↓
LOG alert sent
```

### High Error Rate

```text
5 ERROR signals within 60 seconds
        ↓
HIGH_ERROR_RATE incident
        ↓
High Error Rate Runbook
        ↓
LOG alert sent
```

### Timeout Spike

```text
3 TIMEOUT signals within 60 seconds
        ↓
TIMEOUT_SPIKE incident
        ↓
Dependency Failure / Timeout Spike Runbook
        ↓
LOG alert sent
```

---

## Demo Scenarios

### Simulate High Error Rate

Set the demo payment service to always fail:

```http
POST /simulate/failure-mode
```

Body:

```json
{
  "mode": "RANDOM_FAILURES",
  "latencyMs": 0,
  "failureRate": 1.0
}
```

Then send five payment requests.

Expected result:

```text
HIGH_ERROR_RATE incident
severity HIGH
alert SENT
```

---

### Simulate Timeout Spike

Set the demo payment service to timeout:

```http
POST /simulate/failure-mode
```

Body:

```json
{
  "mode": "TIMEOUTS",
  "latencyMs": 2500,
  "failureRate": 1.0
}
```

Then send three payment requests.

Expected result:

```text
TIMEOUT_SPIKE incident
severity HIGH
alert SENT
```

---

### Recover Demo Service

```http
POST /simulate/recovery
```

This restores the demo service to normal behavior.

---

## API Endpoints Added in V3

### Timeline

```http
GET /incidents/{incidentId}/timeline
```

Returns ordered timeline events for an incident.

### Alerts

```http
GET /incidents/{incidentId}/alerts
```

Returns alerts created for an incident.

### Dashboard

```http
GET /dashboard/summary
```

Returns operational counters for incidents and alerts.

---

## Persistence Added in V3

V3 adds these tables:

```text
incident_timeline_events
alerts
```

### `incident_timeline_events`

Stores the operational history of each incident.

### `alerts`

Stores alert delivery state.

---

## Worker Responsibilities Added in V3

The worker now handles:

- signal task processing
- rule evaluation
- incident creation
- evidence creation
- timeline event recording
- alert request creation
- pending alert processing
- log alert sending

---

## Current Status

### Version 3 complete

Implemented features:

- incident timeline domain model
- alert domain model
- runbook reference model
- timeline and alert persistence schema
- timeline and alert JPA adapters
- timeline events for incident open, evidence attached, acknowledge and resolve
- timeline API endpoint
- alert request creation when incidents open
- log alert sender
- alert delivery processor
- incident alerts API endpoint
- operational dashboard summary endpoint
- initial runbooks
- end-to-end validation for latency, error rate and timeout incidents

---

## Known Limitations

V3 intentionally does not include:

- Slack integration
- email integration
- webhook integration
- AWS SNS integration
- alert retries
- alert suppression rules
- escalation policies
- on-call schedules
- UI dashboard
- incident ownership
- manual notes
- automatic incident reopening
- real metrics ingestion

These belong to later versions.

---

## Next Version

### V4 – External Integrations & Production Hardening

Potential V4 focus:

- webhook alert sender
- Slack or SNS integration
- alert retry policy
- failed alert recovery
- incident ownership
- manual timeline notes
- configurable rule management
- improved dashboard
- OpenTelemetry tracing
- metrics endpoint
- production Docker profiles

---

V3 is the version where IncidentHub starts looking like a real incident response platform.

It connects detection, explanation, runbooks, alerting and operational visibility.
