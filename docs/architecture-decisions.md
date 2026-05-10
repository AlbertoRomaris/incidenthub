# IncidentHub – Architecture Decisions

This document records the main architecture decisions made during the development of IncidentHub.

The goal is not only to describe the final architecture, but also to explain why certain decisions were taken.

---

## ADR-001 – Use a Clean / Hexagonal Architecture

### Decision

IncidentHub uses a clean architecture style with a domain/application core separated from infrastructure and delivery mechanisms.

Main modules:

```text
incidenthub-core
incidenthub-infrastructure
incidenthub-api
incidenthub-worker
```

### Reasoning

The incident detection domain should not depend directly on Spring, HTTP, JPA, AWS or database details.

The core should contain:

- domain models
- value objects
- use cases
- ports/interfaces

Infrastructure should contain:

- JPA adapters
- repository implementations
- alert sender implementations
- persistence entities
- external integrations

### Benefits

- easier testing
- clearer boundaries
- less framework coupling
- easier evolution from local to AWS
- better portfolio demonstration of architecture

### Trade-offs

- more files and modules
- more explicit mapping between domain and persistence
- slightly higher initial complexity

---

## ADR-002 – Split API and Worker into Separate Services

### Decision

IncidentHub runs the API and Worker as separate Spring Boot applications.

```text
incidenthub-api
incidenthub-worker
```

### Reasoning

The API is responsible for external HTTP traffic and signal ingestion.

The Worker is responsible for asynchronous processing, rule evaluation and alert delivery.

These workloads have different scaling and operational characteristics.

### Benefits

- API can stay responsive
- rule processing can evolve independently
- Worker can be scaled separately
- production-like architecture
- better fit for ECS/Fargate deployment

### Trade-offs

- more deployment units
- more Docker images
- more ECS services
- more local orchestration complexity

---

## ADR-003 – Use PostgreSQL as the Initial Source of Truth

### Decision

PostgreSQL is used as the source of truth for:

- signals
- rules
- processing tasks
- incidents
- evidence
- timeline events
- alerts
- metrics-derived state

### Reasoning

PostgreSQL is simple, reliable and familiar. It works well locally through Docker Compose and in AWS through RDS.

For the current project scope, PostgreSQL-backed processing tasks are enough to demonstrate asynchronous behavior.

### Benefits

- simple local development
- reliable persistence
- easy inspection
- fewer moving parts in early versions
- works well with Spring Data JPA

### Trade-offs

- not as scalable as queue-native processing for very high throughput
- polling-based processing is less elegant than event streaming
- SQS integration remains a future improvement for runtime processing

---

## ADR-004 – Use Processing Tasks Instead of Direct Inline Rule Evaluation

### Decision

When the API receives a signal, it persists the signal and creates a processing task. The Worker later processes that task.

### Reasoning

Signal ingestion and incident detection should be decoupled.

The API should not block too long while evaluating rules, creating incidents, attaching evidence and sending alerts.

### Benefits

- asynchronous design
- better separation of responsibilities
- production-like processing model
- easier to extend toward SQS later

### Trade-offs

- requires task state management
- requires Worker polling
- processing is eventually consistent

---

## ADR-005 – Use Deduplication Keys for Active Incidents

### Decision

IncidentHub deduplicates active incidents using a key derived from:

```text
environment + serviceName + incidentType
```

### Reasoning

Operational systems should avoid creating one incident per signal when the signals represent the same ongoing issue.

### Benefits

- reduces alert noise
- keeps incident list readable
- increments occurrence count on repeated matches
- models real incident management behavior

### Trade-offs

- deduplication logic must be carefully designed
- different rules may need different deduplication strategies in the future

---

## ADR-006 – Attach Evidence to Incidents

### Decision

When a rule matches, the matching signal is stored as incident evidence.

### Reasoning

An incident should explain why it exists.

Evidence connects rule evaluation with the source signals that caused the incident.

### Benefits

- better traceability
- easier debugging
- more useful incident detail
- stronger demonstration of incident lifecycle

### Trade-offs

- more storage
- more domain objects and endpoints

---

## ADR-007 – Record Timeline Events

### Decision

IncidentHub records incident timeline events for important actions.

Examples:

- incident opened
- runbook attached
- alert requested
- evidence attached
- alert sent
- alert failed

### Reasoning

Incident response is chronological. A timeline makes the incident understandable.

### Benefits

- auditability
- operational clarity
- better UI potential
- stronger portfolio storytelling

### Trade-offs

- extra writes
- extra table/model
- needs careful event naming

---

## ADR-008 – Model Runbooks as References

### Decision

Runbooks are represented as references attached to incidents, not as full editable documents inside the database.

### Reasoning

For this project, runbooks are static operational guidance documents stored in the repository.

The system only needs to attach the right runbook reference to the incident.

### Benefits

- simple implementation
- versionable runbooks in Git
- easy to document
- enough for portfolio scope

### Trade-offs

- no dynamic runbook editing
- no runbook ownership workflow
- no external knowledge base integration yet

---

## ADR-009 – Add Metrics and SLOs Before AWS

### Decision

Operational metrics and SLO summaries were added before the full AWS deployment.

### Reasoning

Cloud deployment is more meaningful when the application already exposes operational health indicators.

### Benefits

- easier AWS validation
- more realistic SRE story
- useful CloudWatch/Prometheus demonstration
- enables dashboard and future UI

### Trade-offs

- metrics are application-level and relatively simple
- no distributed tracing yet
- no managed Prometheus/Grafana integration yet

---

## ADR-010 – Use Terraform for AWS Infrastructure

### Decision

AWS infrastructure is provisioned with Terraform.

### Reasoning

Infrastructure should be reproducible, reviewable and documented as code.

### Benefits

- repeatable deployments
- easy destroy/recreate
- clear infrastructure documentation
- strong DevOps/Cloud portfolio value

### Trade-offs

- state management must be considered
- Terraform adds complexity
- AWS resources may create cost if not destroyed

---

## ADR-011 – Use ECS Fargate for API and Worker

### Decision

IncidentHub uses ECS Fargate for cloud runtime compute.

### Reasoning

Fargate avoids managing EC2 instances while still demonstrating containerized cloud deployment.

### Benefits

- serverless containers
- production-like deployment model
- suitable for API and Worker services
- integrates with ALB, IAM, CloudWatch and ECR

### Trade-offs

- costs can accumulate if services remain running
- networking and IAM configuration are more involved
- cold starts/redeploys are slower than local Docker

---

## ADR-012 – Use RDS PostgreSQL in AWS

### Decision

AWS deployment uses Amazon RDS PostgreSQL.

### Reasoning

The local system already uses PostgreSQL. RDS provides a managed PostgreSQL-compatible cloud database.

### Benefits

- managed database
- familiar SQL/JPA behavior
- realistic cloud architecture
- durable state

### Trade-offs

- RDS creates ongoing cost
- startup/destruction takes time
- free tier limitations can affect backup configuration

---

## ADR-013 – Keep SQS as Cloud Foundation Before Full Runtime Integration

### Decision

V5 provisions SQS and DLQ foundations, but the current runtime still uses PostgreSQL-backed processing tasks.

### Reasoning

SQS is important for the target cloud architecture, but integrating it fully would be a separate architectural change.

The project first validates AWS deployment and operational capabilities before replacing the task model.

### Benefits

- cloud messaging foundation exists
- dashboard and alarms can include SQS/DLQ
- future queue-based runtime is prepared

### Trade-offs

- SQS metrics remain mostly flat for now
- architecture is not fully queue-native yet
- documentation must clearly explain current vs future behavior

---

## ADR-014 – Use SNS for Real Alert Delivery

### Decision

V6 implements SNS alert delivery for the cloud runtime.

### Reasoning

A real alert delivery path is more valuable than only logging alerts.

SNS allows IncidentHub to demonstrate external cloud notification delivery.

### Benefits

- real AWS integration
- email delivery validation
- useful operational demo
- confirms Worker can publish to AWS services

### Trade-offs

- requires IAM permissions
- requires topic ARN configuration
- email subscriptions must be confirmed

---

## ADR-015 – Make Alert Channel Configurable

### Decision

The alert channel is configurable through application properties.

Local/default:

```text
LOG
```

AWS cloud:

```text
SNS
```

### Reasoning

The core use case should not hardcode infrastructure-specific behavior.

### Benefits

- local development remains simple
- cloud behavior can use SNS
- no duplicated use cases
- better clean architecture boundary

### Trade-offs

- more configuration
- mismatched channel/sender configuration can fail if misconfigured

---

## ADR-016 – Use GitHub Actions OIDC Instead of Static AWS Keys

### Decision

GitHub Actions authenticates to AWS using OIDC and an IAM role.

### Reasoning

Long-lived AWS credentials in GitHub secrets are less secure and harder to rotate.

OIDC provides short-lived credentials scoped to the repository and branch.

### Benefits

- no static AWS keys in GitHub
- better security posture
- production-like CI/CD setup
- clear IAM trust policy

### Trade-offs

- more IAM setup
- OIDC trust conditions must be correct
- initial debugging can be more complex

---

## ADR-017 – Use GitHub Actions to Build, Push and Redeploy

### Decision

V6 adds a GitHub Actions workflow to build Docker images, push them to ECR and redeploy ECS services.

### Reasoning

Manual deployment is error-prone and less professional.

The project should demonstrate an automated delivery path.

### Benefits

- CI/CD automation
- repeatable deploys
- less manual work
- strong portfolio value

### Trade-offs

- workflow runtime takes time
- permissions must be managed carefully
- using mutable `dev` tags requires `force-new-deployment`

---

## ADR-018 – Use Mutable `dev` Tag for Simplicity

### Decision

The current deployment uses the `dev` Docker image tag.

### Reasoning

For a single-environment portfolio deployment, this is simple and easy to understand.

### Benefits

- simple ECR workflow
- simple ECS task definition
- easy local and CI publishing

### Trade-offs

- less traceability than Git SHA tags
- ECS must be forced to redeploy
- not ideal for production release promotion

### Future Improvement

Use Git SHA tags:

```text
incidenthub-dev-api:<commit-sha>
incidenthub-dev-worker:<commit-sha>
```

and update ECS task definitions with immutable image references.

---

## ADR-019 – Destroy AWS Resources When Not Needed

### Decision

AWS resources are destroyed after validation/demo sessions to avoid costs.

### Reasoning

RDS, ALB, ECS and CloudWatch can generate cost if left running.

### Benefits

- cost control
- safer experimentation
- reinforces Terraform reproducibility

### Trade-offs

- environment must be recreated for demos
- ECR images may need to be repushed
- historical runtime data is removed

---

## Summary

IncidentHub prioritizes:

- clear domain boundaries
- asynchronous operational processing
- explainable incidents
- real cloud deployment
- observability
- automated delivery
- cost-aware AWS usage

Some production features are intentionally left for future versions:

- remote Terraform backend
- immutable image tags
- HTTPS/custom domain
- SQS runtime processing
- autoscaling
- distributed tracing
- frontend UI
