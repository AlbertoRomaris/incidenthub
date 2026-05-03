# V2 – Async Rule Evaluation & Incident Lifecycle

## Overview

IncidentHub V2 evolves the platform from a **local signal ingestion system** into an **asynchronous incident detection platform**.

V1 proved that external services can emit operational signals and that IncidentHub can receive, validate, persist and query them.

V2 introduces the next architectural layer:

- asynchronous signal processing
- database-backed processing tasks
- rule evaluation
- incident creation
- incident deduplication
- incident evidence
- incident lifecycle operations

The focus of this version is not cloud infrastructure yet.

The focus is to transform raw operational signals into actionable operational state.

---

## Why V2 Exists

V1 stored operational signals, but it did not interpret them.

That means V1 could answer:

```text
What signals were received?
```

V2 starts answering more useful operational questions:

```text
Is this service currently degraded?
Which rule was triggered?
Which signals caused the incident?
Is the incident still open?
Has someone acknowledged it?
Was it resolved?
```

This version moves IncidentHub from:

```text
signal storage
```

to:

```text
signal interpretation and incident management
```

The central idea is:

```text
single signal ≠ incident
pattern of signals = possible incident
```

---

## Core Architectural Principles

- Signals are raw operational observations.
- Incidents are derived operational state.
- The API ingests signals but does not evaluate rules.
- Rule evaluation happens asynchronously in the Worker.
- Processing state is stored outside the `signals` table.
- Incidents are deduplicated to reduce alert noise.
- Evidence explains why an incident exists.
- Lifecycle operations model real operational workflows.
- The core remains independent from Spring, JPA and PostgreSQL.

---

## High-Level Architecture

V2 introduces a separate Worker process and a database-backed processing queue.

```text
Demo Payment Service
        |
        | POST /signals
        v
IncidentHub API
        |
        | 1. Persist Signal
        | 2. Create SignalProcessingTask(PENDING)
        v
PostgreSQL
        |
        | claim pending task
        v
IncidentHub Worker
        |
        | evaluate enabled rules
        v
Incident Repository
        |
        | create/update incident
        | attach evidence
        v
PostgreSQL
```

The API remains responsible for fast ingestion.

The Worker becomes responsible for interpretation.

---

## Key Difference vs V1

In V1:

```text
signal → persisted signal
```

In V2:

```text
signal → processing task → rule evaluation → incident/evidence
```

V1 made signals visible.

V2 makes signals actionable.

---

## Database-backed Signal Processing Tasks

V2 introduces the `signal_processing_tasks` table.

This table represents the internal processing state of each signal.

The important design decision is that processing state is not stored inside the `signals` table.

```text
signals
    raw operational observations

signal_processing_tasks
    internal worker processing state
```

This keeps the signal model clean and allows the Worker to manage retries, locks and processing state independently.

---

## Processing Task Lifecycle

Each signal has a processing task.

```text
PENDING
   |
   v
PROCESSING
   |
   +--> PROCESSED
   |
   +--> FAILED
```

### Status meanings

- **PENDING**  
  The signal has been accepted and is waiting to be processed.

- **PROCESSING**  
  A Worker has claimed the task.

- **PROCESSED**  
  The signal was evaluated successfully.

- **FAILED**  
  Processing failed and the error was stored.

---

## Worker Polling & Locking

The Worker periodically claims pending tasks.

The infrastructure uses database locking with:

```sql
FOR UPDATE SKIP LOCKED
```

This allows safe processing even if multiple workers are running.

The claim operation records:

- worker id
- locked timestamp
- attempt count
- processing status

This gives the system a production-style processing model while still running fully locally.

---

## Rule Model

A rule defines when a group of signals should be interpreted as an incident.

Current rule fields include:

- rule id
- name
- description
- service pattern
- signal type
- condition type
- threshold
- time window
- incident type
- incident severity
- enabled flag

Current condition type used in V2:

```text
COUNT_OVER_WINDOW
```

Example rule:

```text
High latency on payment-service
signalType: LATENCY
conditionType: COUNT_OVER_WINDOW
threshold: 3
timeWindowSeconds: 60
incidentType: HIGH_LATENCY
incidentSeverity: MEDIUM
```

Meaning:

```text
If payment-service emits at least 3 LATENCY signals within 60 seconds,
open or update a HIGH_LATENCY incident.
```

---

## Initial Rules

V2 includes three initial rules.

### High Error Rate

```text
servicePattern: payment-service
signalType: ERROR
conditionType: COUNT_OVER_WINDOW
threshold: 5
timeWindowSeconds: 60
incidentType: HIGH_ERROR_RATE
incidentSeverity: HIGH
```

### High Latency

```text
servicePattern: payment-service
signalType: LATENCY
conditionType: COUNT_OVER_WINDOW
threshold: 3
timeWindowSeconds: 60
incidentType: HIGH_LATENCY
incidentSeverity: MEDIUM
```

### Timeout Spike

```text
servicePattern: payment-service
signalType: TIMEOUT
conditionType: COUNT_OVER_WINDOW
threshold: 3
timeWindowSeconds: 60
incidentType: TIMEOUT_SPIKE
incidentSeverity: HIGH
```

---

## Rule Evaluation Flow

The Worker processes one signal at a time.

For each signal:

1. Load the signal by id.
2. Load enabled rules.
3. Select rules matching:
    - service name
    - signal type
4. Evaluate the rule over the configured time window.
5. If the rule does not match, mark the task as processed.
6. If the rule matches:
    - create or update an incident
    - attach incident evidence
    - mark the task as processed

The Worker does not create an incident for every signal.

It creates or updates incidents only when a rule condition is satisfied.

---

## Incident Model

An incident is derived operational state.

It is created by IncidentHub, not by the producing service.

Current incident fields include:

- incident id
- service name
- environment
- incident type
- severity
- status
- summary
- description
- deduplication key
- occurrence count
- first seen timestamp
- last seen timestamp
- opened timestamp
- acknowledged timestamp
- resolved timestamp

---

## Incident Deduplication

IncidentHub does not create a new incident for every matching signal.

Instead, it deduplicates active incidents using a deduplication key.

Current deduplication key format:

```text
environment:serviceName:incidentType
```

Example:

```text
local:payment-service:high_latency
```

When a rule matches:

```text
if active incident exists for deduplication key:
    update occurrence count
    update lastSeenAt
    attach evidence

else:
    create new OPEN incident
    attach evidence
```

This models a real operational concern:

```text
many noisy signals → one actionable incident
```

---

## Incident Evidence

Evidence explains why an incident exists.

Each evidence record links:

- incident
- signal
- rule
- captured timestamp
- summary
- contextual attributes

Example evidence attributes:

```json
{
  "serviceName": "payment-service",
  "environment": "local",
  "signalType": "LATENCY",
  "severity": "MEDIUM",
  "correlationId": "checkout-req-v2-002",
  "matchingSignalsCount": 3,
  "reason": "Matched 3 signals within 60 seconds"
}
```

This makes incidents explainable.

The system can answer:

```text
Which rule triggered this incident?
Which signal confirmed the match?
How many signals matched the rule?
Which correlation id was involved?
```

---

## Incident Lifecycle

V2 introduces a basic operational lifecycle.

```text
OPEN → ACKNOWLEDGED → RESOLVED
```

### State meanings

- **OPEN**  
  The incident was created by a rule match and still requires attention.

- **ACKNOWLEDGED**  
  Someone has seen the incident and accepted ownership.

- **RESOLVED**  
  The incident is no longer active.

Resolved incidents no longer appear in the default active incident list, but they remain queryable by id and by status filter.

---

## Lifecycle Operations

V2 exposes explicit lifecycle operations.

```http
POST /incidents/{incidentId}/acknowledge
POST /incidents/{incidentId}/resolve
```

These operations are intentionally explicit.

They model operational behavior rather than simply updating a database record.

---

## API Endpoints

### Signals

`POST /signals`

Receives an operational signal and creates a pending processing task.

`GET /signals`

Lists recent signals.

`GET /signals/{signalId}`

Returns a signal by id.

---

### Rules

`GET /rules`

Lists enabled rules.

`GET /rules/{ruleId}`

Returns a rule by id.

---

### Incidents

`GET /incidents`

Lists active incidents.

By default, active incidents include:

```text
OPEN
ACKNOWLEDGED
```

`GET /incidents?status=OPEN`

Lists open incidents.

`GET /incidents?status=ACKNOWLEDGED`

Lists acknowledged incidents.

`GET /incidents?status=RESOLVED`

Lists resolved incidents.

`GET /incidents/{incidentId}`

Returns one incident by id.

`GET /incidents/{incidentId}/evidence`

Returns the evidence attached to an incident.

`POST /incidents/{incidentId}/acknowledge`

Acknowledges an open incident.

`POST /incidents/{incidentId}/resolve`

Resolves an open or acknowledged incident.

---

## Demo Scenario

A typical V2 demo is:

1. Start PostgreSQL.
2. Start IncidentHub API.
3. Start IncidentHub Worker.
4. Start Demo Payment Service.
5. Set the payment service to high latency mode.
6. Execute three payment requests.
7. The payment service emits three `LATENCY` signals.
8. IncidentHub API stores the signals and creates pending processing tasks.
9. Worker processes the tasks.
10. The high latency rule matches.
11. IncidentHub opens one `HIGH_LATENCY` incident.
12. Evidence is attached to the incident.
13. The incident is acknowledged.
14. The incident is resolved.

Expected result:

```text
3 LATENCY signals
        ↓
1 HIGH_LATENCY incident
        ↓
evidence attached
        ↓
OPEN → ACKNOWLEDGED → RESOLVED
```

---

## Example Resulting Incident

```text
incidentType     : HIGH_LATENCY
serviceName      : payment-service
environment      : local
severity         : MEDIUM
status           : OPEN
summary          : HIGH_LATENCY detected for payment-service
occurrenceCount  : 2
deduplicationKey : local:payment-service:high_latency
```

Example evidence:

```text
summary              : Signal matched rule: High latency on payment-service
correlationId        : checkout-req-v2-002
matchingSignalsCount : 3
reason               : Matched 3 signals within 60 seconds
```

---

## Persistence

V2 adds these tables:

```text
rules
incidents
incident_evidence
signal_processing_tasks
```

### `rules`

Stores enabled operational rules.

### `incidents`

Stores deduplicated operational incidents.

### `incident_evidence`

Stores why an incident exists.

### `signal_processing_tasks`

Stores internal Worker processing state for each signal.

---

## Project Structure

```text
incidenthub/
├── incidenthub-core/
│   ├── domain/
│   │   ├── signal/
│   │   ├── rule/
│   │   ├── incident/
│   │   └── evidence/
│   └── application/
│       ├── model/
│       ├── port/
│       └── usecase/
│
├── incidenthub-infrastructure/
│   ├── persistence/
│   │   ├── signal/
│   │   ├── rule/
│   │   ├── incident/
│   │   ├── evidence/
│   │   └── task/
│   └── resources/db/migration/
│
├── incidenthub-api/
│   ├── controller/
│   ├── dto/
│   └── config/
│
├── incidenthub-worker/
│   ├── processor/
│   └── config/
│
├── demo-services/
│   └── demo-payment-service/
│
├── docs/
├── requests/
├── docker-compose.yml
└── pom.xml
```

---

## Responsibility Split

### IncidentHub API

The API is responsible for:

- receiving signals
- validating requests
- creating signal processing tasks
- exposing signal queries
- exposing rule queries
- exposing incident queries
- exposing incident lifecycle operations

The API does not evaluate rules.

---

### IncidentHub Worker

The Worker is responsible for:

- claiming pending signal processing tasks
- loading signals
- loading enabled rules
- evaluating rules
- creating or updating incidents
- attaching evidence
- marking tasks as processed or failed

The Worker is the only component responsible for signal interpretation.

---

### IncidentHub Core

The core contains:

- signal domain model
- rule domain model
- incident domain model
- evidence domain model
- use cases
- ports

The core does not depend on Spring, JPA, PostgreSQL or Docker.

---

### IncidentHub Infrastructure

Infrastructure contains:

- JPA entities
- Spring Data repositories
- Flyway migrations
- PostgreSQL adapters
- database-backed task claiming

Infrastructure implements core ports.

---

## Current Status

### Version 2 complete

Implemented features:

- database-backed signal processing tasks
- separate Worker application
- scheduled Worker polling
- safe task claiming with database locking
- rule domain model
- incident domain model
- evidence domain model
- rule persistence
- incident persistence
- evidence persistence
- rule evaluation use case
- incident deduplication
- incident evidence
- incident lifecycle
- rule query endpoints
- incident query endpoints
- incident status filtering

---

## Known Limitations

V2 intentionally does not include:

- external queue
- AWS SQS
- SNS alerting
- alert suppression
- escalation policies
- OpenTelemetry tracing
- Prometheus metrics
- dashboard UI
- automatic stale lock recovery
- advanced rule types
- rule management endpoints
- incident reopen flow

These concerns belong to later versions.

---

## Next Version

### V3 – Alerting, Runbooks & Operational Workflows

V3 will evolve incidents into richer operational workflows.

Planned additions:

- alerting adapter
- mock/log alert sender
- runbook association by incident type
- incident lifecycle events
- incident timeline
- alert delivery state
- dashboard summary endpoint

The goal of V3 is to make incidents not only detectable, but actionable.

---

V2 is the first version where IncidentHub behaves like an operational platform.

It turns raw signals into deduplicated incidents with evidence and lifecycle state.