# IncidentHub – Runbooks Overview

This document explains the runbook concept used by IncidentHub.

Runbooks are operational response guides associated with incident types.

They help bridge the gap between incident detection and incident response.

---

## What is a Runbook?

A runbook is a structured document that explains how to investigate, mitigate and resolve a specific type of operational issue.

Examples:

- high latency
- high error rate
- timeout spike
- dependency failure
- queue backlog

IncidentHub does not only detect incidents. It also attaches the most relevant runbook reference to help the operator decide what to do next.

---

## How IncidentHub Uses Runbooks

When an incident is opened, the Worker checks whether a runbook is available for that incident type.

If a runbook exists, IncidentHub records a timeline event:

```text
RUNBOOK_ATTACHED
```

The timeline event includes:

- incident type
- runbook title
- runbook document path
- runbook summary

Example:

```text
Incident type: HIGH_LATENCY
Runbook: High Latency Runbook
Path: docs/runbooks/high-latency.md
```

---

## Why Runbooks Matter

Runbooks make incidents more actionable.

Without a runbook:

```text
The system says something is wrong.
```

With a runbook:

```text
The system says something is wrong and points to the response procedure.
```

This is closer to how real SRE and operations teams work.

---

## Current Runbook Model

Runbooks are currently static references stored in code and documentation.

They are not database-managed documents.

This keeps the implementation simple and version-controlled.

Current approach:

```text
incident type → runbook reference
```

Example:

```text
HIGH_LATENCY → docs/runbooks/high-latency.md
HIGH_ERROR_RATE → docs/runbooks/high-error-rate.md
TIMEOUT_SPIKE → docs/runbooks/timeout-spike.md
```

---

## Runbook Timeline Event

When a runbook is attached, the incident timeline records:

```text
eventType: RUNBOOK_ATTACHED
actor: system
summary: Runbook attached: <title>
```

Example attributes:

```json
{
  "incidentType": "HIGH_LATENCY",
  "title": "High Latency Runbook",
  "documentPath": "docs/runbooks/high-latency.md",
  "summary": "Investigate latency degradation and slow downstream responses."
}
```

This makes the incident timeline self-explanatory.

---

## Recommended Runbook Structure

Each runbook should contain:

```text
1. Purpose
2. Symptoms
3. Immediate checks
4. Investigation steps
5. Mitigation steps
6. Escalation criteria
7. Useful queries or commands
8. Related metrics
9. Prevention ideas
```

---

## Example Runbook Flow

For a high latency incident:

```text
HIGH_LATENCY incident opened
        ↓
Runbook attached
        ↓
Operator checks service latency
        ↓
Operator checks database and downstream dependencies
        ↓
Operator checks recent deployments
        ↓
Operator mitigates or escalates
```

---

## Current Benefits

The runbook integration demonstrates:

- operational thinking
- incident response workflow
- better context for incidents
- connection between detection and mitigation
- timeline-based auditability

---

## Limitations

Current runbook implementation does not include:

- editable runbooks through API
- runbook ownership
- runbook versioning in the database
- external knowledge base integration
- automatic action execution
- approval workflows

These are intentionally out of scope for the current project.

---

## Future Improvements

Possible future runbook enhancements:

- expose runbook content through API
- show runbooks in a UI
- add runbook severity-specific sections
- link runbooks to dashboards
- add runbook checklists
- track manual mitigation steps
- integrate with external documentation platforms
- add ownership and escalation metadata

---

## Portfolio Value

Runbooks are useful for demonstrating that IncidentHub is not just a signal-processing system.

It models a more complete operational response workflow:

```text
Detect
  ↓
Open incident
  ↓
Attach evidence
  ↓
Attach runbook
  ↓
Send alert
  ↓
Expose timeline
```

This makes the project more realistic and easier to explain.
