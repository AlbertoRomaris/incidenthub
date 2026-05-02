# V1 – Local Signal Ingestion Platform

## Overview

IncidentHub V1 is the first version of the platform and focuses on **operational signal ingestion**.

The goal of this version is not to detect incidents yet, nor to implement a full SRE platform from the beginning.

V1 establishes the foundation required to receive, validate, persist and inspect operational signals emitted by external services.

A signal represents a technical observation from a system, such as:

- an error
- high latency
- a timeout
- a dependency failure
- a service-down condition
- a custom operational event

This version proves the basic flow:

```text
Demo Service
    |
    | emits operational signal
    v
IncidentHub API
    |
    | persists signal
    v
PostgreSQL
```

The project is intentionally simple in this version, but strict in design.

The focus is to build a clean foundation for later versions where signals will be processed asynchronously, evaluated by rules and converted into actionable incidents.

---

## Core Principles

- Operational signals are raw observations, not incidents.
- IncidentHub must ingest signals without knowing the internals of the producing service.
- The core domain must remain independent of Spring, JPA and PostgreSQL.
- Persistence is implemented through an infrastructure adapter.
- The API should stay thin and delegate business logic to use cases.
- Local development should be reproducible through Docker Compose.
- V1 should be easy to run, verify and explain.

---

## High-Level Architecture

V1 uses a synchronous ingestion flow.

```text
Client
  |
  | POST /payments/charge
  v
Demo Payment Service
  |
  | POST /signals
  v
IncidentHub API
  |
  | IngestSignalUseCase
  v
SignalRepository Port
  |
  | JPA Adapter
  v
PostgreSQL
```

The demo payment service acts as an external producer.

IncidentHub receives signals through HTTP, maps them into application commands, creates domain `Signal` objects and persists them in PostgreSQL.

---

## Signal Model

A signal is an operational observation emitted by a service.

Current signal fields include:

- signal id
- service name
- environment
- signal type
- severity
- message
- correlation id
- trace id
- span id
- latency
- HTTP status code
- error code
- observed timestamp
- received timestamp
- custom attributes

### Signal Types

Current supported signal types:

```text
ERROR
LATENCY
TIMEOUT
HEARTBEAT
DEPENDENCY_FAILURE
QUEUE_BACKLOG
DEPLOYMENT_EVENT
CUSTOM
```

### Signal Severities

Current supported severities:

```text
LOW
MEDIUM
HIGH
CRITICAL
```

### Example Signal

```json
{
  "serviceName": "payment-service",
  "environment": "local",
  "signalType": "LATENCY",
  "severity": "MEDIUM",
  "message": "High payment latency detected",
  "correlationId": "checkout-req-001",
  "latencyMs": 2500,
  "statusCode": 200,
  "errorCode": "PAYMENT_HIGH_LATENCY",
  "timestamp": "2026-05-02T20:14:42Z",
  "attributes": {
    "mode": "HIGH_LATENCY"
  }
}
```

---

## Ingestion Flow

The ingestion flow is intentionally simple in V1.

1. A producer sends a signal to `POST /signals`.
2. The API validates the request payload.
3. The controller maps the request to an application command.
4. `IngestSignalUseCase` creates a domain `Signal`.
5. The signal is persisted through the `SignalRepository` port.
6. The JPA adapter stores the signal in PostgreSQL.
7. The API returns `202 Accepted`.

This keeps HTTP concerns outside the domain and persistence concerns outside the core.

---

## Demo Payment Service

V1 includes a demo payment service.

The purpose of this service is not to model a real payment system.

Its purpose is to generate observable behavior that IncidentHub can ingest.

The demo payment service can simulate:

- normal payments
- high latency
- random failures
- timeouts
- service unavailable behavior

When a simulated operational condition occurs, the service emits a signal to IncidentHub.

Example:

```text
POST /simulate/failure-mode
mode = HIGH_LATENCY

POST /payments/charge
        |
        v
payment-service emits LATENCY signal
        |
        v
IncidentHub persists signal
```

This allows the project to demonstrate signal ingestion with a real producer instead of only manual HTTP requests.

---

## API Endpoints

### IncidentHub API

`POST /signals`

Receives an operational signal.

`GET /signals`

Returns recent signals.

Optional query parameter:

```text
limit
```

Example:

```http
GET /signals?limit=10
```

`GET /signals/{signalId}`

Returns a single signal by id.

`GET /actuator/health`

Health check endpoint.

---

### Demo Payment Service

`POST /payments/charge`

Simulates a payment charge.

Depending on the configured failure mode, this may emit an operational signal to IncidentHub.

`GET /simulate/failure-mode`

Returns the current simulation mode.

`POST /simulate/failure-mode`

Changes the simulation mode.

Example:

```json
{
  "mode": "HIGH_LATENCY",
  "latencyMs": 2500,
  "failureRate": 0.0
}
```

`POST /simulate/recovery`

Resets the demo payment service to normal behavior.

`GET /actuator/health`

Health check endpoint.

---

## Persistence

Signals are stored in PostgreSQL.

The database schema is managed with Flyway.

Current table:

```text
signals
```

The table stores:

- signal metadata
- service and environment
- type and severity
- correlation fields
- latency and status information
- timestamps
- custom JSON attributes

Indexes are created for fields useful during investigation and future rule evaluation:

- service and environment
- signal type
- severity
- observed timestamp
- correlation id

---

## Project Structure

```text
incidenthub/
├── incidenthub-core/
├── incidenthub-infrastructure/
├── incidenthub-api/
├── incidenthub-worker/
├── demo-services/
│   └── demo-payment-service/
├── docs/
├── requests/
├── docker-compose.yml
└── pom.xml
```

### `incidenthub-core`

Contains pure domain and application logic.

Current responsibilities:

- signal domain model
- signal ingestion use case
- repository port

The core does not depend on Spring, JPA or PostgreSQL.

### `incidenthub-infrastructure`

Contains technical adapters.

Current responsibilities:

- Flyway migration
- JPA signal entity
- Spring Data repository
- JPA adapter implementing the core repository port

### `incidenthub-api`

HTTP adapter for IncidentHub.

Current responsibilities:

- expose signal endpoints
- validate requests
- map HTTP DTOs to application commands
- wire use cases as Spring beans

### `incidenthub-worker`

Prepared worker module.

It does not process signals yet.

It exists because future versions will move interpretation and rule evaluation into a separate worker process.

### `demo-payment-service`

External demo producer.

Current responsibilities:

- simulate payment behavior
- simulate operational failures
- emit signals to IncidentHub

---

## Local Validation Scenario

A typical V1 demo is:

1. Start PostgreSQL.
2. Start IncidentHub API.
3. Start Demo Payment Service.
4. Set payment service to high latency mode.
5. Charge a payment.
6. Query IncidentHub signals.

Expected result:

- payment request completes with simulated latency
- demo payment service emits a `LATENCY` signal
- IncidentHub stores the signal
- `GET /signals` shows the new signal

Example resulting signal:

```text
serviceName   : payment-service
environment   : local
signalType    : LATENCY
severity      : MEDIUM
message       : High payment latency detected
correlationId : checkout-req-001
latencyMs     : 2500
errorCode     : PAYMENT_HIGH_LATENCY
```

---

## Current Status

### Version 1 complete

Implemented features:

- Maven multi-module backend structure
- Operational signal domain model
- Signal ingestion use case
- PostgreSQL persistence
- Flyway database migration
- Signal ingestion endpoint
- Signal query endpoints
- Demo payment service signal producer
- Local Docker Compose PostgreSQL setup
- Health endpoints

---

## Known Limitations

V1 intentionally does not include:

- asynchronous processing
- worker consumption
- rule evaluation
- incident creation
- incident deduplication
- incident evidence
- alerting
- runbook association
- metrics dashboards
- AWS deployment

These concerns belong to later versions.

---

## Next Version

### V2 – Asynchronous Processing & Rule Engine

V2 will introduce asynchronous processing and operational rule evaluation.

Planned additions:

- queue between API and worker
- signal processing worker
- rule definitions
- first rule evaluation engine
- incident creation
- deduplication strategy
- incident evidence

The goal of V2 is to move from storing raw signals to interpreting operational patterns.

---

V1 keeps the scope intentionally small.

It establishes a clean ingestion foundation that later versions can build on without rewriting the core architecture.