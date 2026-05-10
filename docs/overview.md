# IncidentHub – Project Overview

## What is IncidentHub?

IncidentHub is a cloud-ready incident detection and operational response platform built with Java, Spring Boot and AWS.

The project models how an incident management system can evolve from a local clean-architecture backend into a production-like cloud platform with asynchronous processing, operational metrics, SLOs, real alert delivery and CI/CD automation.

IncidentHub receives operational signals, evaluates rules, opens or updates incidents, attaches evidence, records timeline events, links runbooks, sends alerts and exposes operational health through metrics and SLO endpoints.

---

## Project Goal

The goal of IncidentHub is to demonstrate a realistic backend/platform engineering project that combines:

- clean architecture
- event-driven processing
- asynchronous workers
- incident lifecycle management
- evidence and timeline tracking
- runbook attachment
- alert delivery
- operational metrics
- SLO summaries
- AWS cloud deployment
- infrastructure as code
- CI/CD automation

The project is designed as a portfolio-grade system, not just a CRUD API.

---

## Main Capabilities

### Signal ingestion

The API receives signals representing operational events such as latency spikes, errors or timeouts.

Example signal types:

- `LATENCY`
- `ERROR`
- `TIMEOUT`

Signals include contextual attributes such as service name, environment, severity, correlation id, latency, status code and additional metadata.

---

### Asynchronous rule processing

Signals are not fully processed inline by the API.

Instead, they are persisted and processed asynchronously by a Worker service. This separates ingestion from rule evaluation and makes the system easier to scale and reason about.

Current runtime model:

```text
API receives signal
        ↓
Signal persisted in PostgreSQL
        ↓
Processing task created
        ↓
Worker polls pending task
        ↓
Worker evaluates matching rules
```

---

### Rule-based incident detection

Rules define when an incident should be opened.

Example:

```text
If payment-service emits 3 LATENCY signals within 60 seconds,
open a HIGH_LATENCY incident.
```

The system supports deduplication, so repeated matching signals update the existing active incident instead of creating duplicated incidents.

---

### Incident lifecycle

IncidentHub supports an operational incident lifecycle:

```text
OPEN
ACKNOWLEDGED
RESOLVED
```

For each incident, the system stores:

- summary
- description
- service name
- environment
- incident type
- severity
- status
- occurrence count
- first seen timestamp
- last seen timestamp
- opened timestamp
- acknowledged timestamp
- resolved timestamp

---

### Evidence

When a rule matches, the signals that caused the incident are attached as evidence.

Evidence includes:

- source signal id
- matching rule id
- captured timestamp
- summary
- contextual attributes

This makes incidents explainable.

---

### Timeline

IncidentHub records timeline events for important incident actions.

Examples:

- `INCIDENT_OPENED`
- `RUNBOOK_ATTACHED`
- `ALERT_REQUESTED`
- `EVIDENCE_ATTACHED`
- `ALERT_SENT`
- `ALERT_FAILED`

The timeline allows a user to understand what happened, when it happened and which component performed the action.

---

### Runbooks

IncidentHub attaches runbook references to incidents based on incident type.

Example:

```text
HIGH_LATENCY → High Latency Runbook
```

Runbooks help connect detection with operational response.

---

### Alerts

IncidentHub creates alerts when incidents are opened.

Supported alert channels:

- `LOG`
- `SNS`

Local/default behavior uses log-based alerting.

AWS cloud behavior uses SNS alert delivery.

---

### Metrics and SLOs

IncidentHub exposes operational metrics and SLO summaries.

Examples:

- signal processing tasks total
- processing success rate
- average processing latency
- pending task backlog
- oldest pending task age
- open incidents
- active incidents
- alerts sent
- alert delivery success rate

SLOs summarize whether important operational indicators are healthy or breached.

---

## Version Evolution

### V1 – Core Incident Detection

Initial domain and API foundations.

Focus:

- signal ingestion
- rule concepts
- incident model
- clean architecture base

---

### V2 – Async Rule Evaluation

Introduced asynchronous processing with a Worker service.

Focus:

- processing tasks
- worker polling
- rule evaluation
- incident opening/updating

---

### V3 – Alerting, Runbooks and Timeline

Expanded incident response capabilities.

Focus:

- incident evidence
- incident timeline
- runbook references
- alert request and delivery workflow

---

### V4 – Metrics, SLOs and Self-Observability

Added operational visibility.

Focus:

- operational metrics
- SLO summaries
- Prometheus endpoint
- health checks

---

### V5 – AWS Cloud Deployment

Deployed the platform in AWS.

Focus:

- Terraform-managed infrastructure
- ECR repositories
- ECS Fargate API and Worker
- RDS PostgreSQL
- ALB public API access
- CloudWatch logs and alarms
- SQS and SNS cloud foundations

---

### V6 – Delivery Automation and Advanced Cloud Operations

Added cloud operational maturity and CI/CD.

Focus:

- CloudWatch runtime dashboard
- SNS alert delivery
- SNS email delivery validation
- GitHub Actions CI
- GitHub Actions OIDC with AWS
- Docker image publishing to ECR
- ECS redeploy after image publishing
- safer ECR publishing script

---

## High-Level Architecture

```text
Clients
  ↓
Application Load Balancer
  ↓
IncidentHub API
  ↓
PostgreSQL / RDS
  ↓
IncidentHub Worker
  ↓
Rule evaluation
  ↓
Incidents, evidence, timeline, alerts
  ↓
SNS / CloudWatch / Metrics / SLOs
```

---

## Local Architecture

```text
Docker Compose
  ├── PostgreSQL
  ├── IncidentHub API
  └── IncidentHub Worker
```

The local setup is useful for development, testing and demonstration without AWS costs.

---

## AWS Architecture

```text
AWS Cloud
  ├── ALB
  ├── ECS Fargate API Service
  ├── ECS Fargate Worker Service
  ├── RDS PostgreSQL
  ├── ECR
  ├── CloudWatch Logs
  ├── CloudWatch Alarms
  ├── CloudWatch Dashboard
  ├── SNS Topic
  └── SQS / DLQ foundations
```

---

## CI/CD Architecture

```text
Push to main
  ↓
GitHub Actions CI
  ↓
Build Java project
  ↓
Assume AWS role through OIDC
  ↓
Build Docker images
  ↓
Push images to ECR
  ↓
Force ECS new deployment
  ↓
Wait for ECS services to become stable
```

---

## Repository Structure

```text
incidenthub/
├── incidenthub-api/
├── incidenthub-worker/
├── incidenthub-core/
├── incidenthub-infrastructure/
├── demo-services/
├── infra/
│   └── aws/
│       ├── bootstrap/
│       └── stack/
├── scripts/
│   └── aws/
├── docs/
├── .github/
│   └── workflows/
└── README.md
```

---

## Why This Project Matters

IncidentHub demonstrates the type of system that goes beyond basic CRUD:

- it has a real domain
- it has asynchronous processing
- it separates domain from infrastructure
- it has operational workflows
- it has cloud infrastructure
- it has observability
- it has alert delivery
- it has CI/CD automation

It is designed to show backend engineering, cloud engineering and SRE-oriented thinking in a single project.
