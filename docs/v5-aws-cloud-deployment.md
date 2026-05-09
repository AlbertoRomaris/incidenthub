# V5 – AWS Cloud Deployment

## Overview

IncidentHub V5 evolves the platform from a locally validated operational system into a cloud-deployed incident response platform.

V4 introduced self-observability through internal metrics, SLO evaluation and a Prometheus-compatible endpoint.

V5 adds the cloud runtime around IncidentHub:

- Terraform-managed AWS infrastructure
- ECR repositories for API and Worker images
- ECS Fargate deployment for the API
- ECS Fargate deployment for the Worker
- public Application Load Balancer
- RDS PostgreSQL persistence
- CloudWatch logs and alarms
- SQS and SNS cloud foundations
- IAM roles for ECS execution and runtime access
- AWS deployment validation script
- end-to-end incident detection validated in AWS

The goal of V5 is to prove that IncidentHub can run as a real cloud platform and process operational signals outside the local development environment.

---

## Why V5 Exists

V4 could answer self-observability questions:

```text
Is IncidentHub processing signals successfully?
Are alerts being delivered?
Are internal SLOs healthy?
Can external monitoring tools scrape metrics?
```

V5 starts answering cloud deployment questions:

```text
Can IncidentHub run in AWS?
Can the API and Worker run as independent cloud services?
Can the API receive public traffic through a load balancer?
Can the platform persist operational state in managed PostgreSQL?
Can the Worker process cloud-ingested signals?
Can AWS logs, alarms and metrics provide operational visibility?
Can a full incident lifecycle be validated in the cloud?
```

This moves IncidentHub from:

```text
observable local incident response platform
```

to:

```text
cloud-deployed operational platform
```

The central idea is:

```text
local architecture is only complete once it runs and proves itself in cloud infrastructure
```

---

## High-Level Flow

```text
HTTP Client
        ↓
Application Load Balancer
        ↓
IncidentHub API on ECS Fargate
        ↓
RDS PostgreSQL
        ↓
IncidentHub Worker on ECS Fargate
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
Metrics and SLOs Updated
        ↓
CloudWatch Logs / Alarms
```

---

## Main V5 Capabilities

### Terraform-managed AWS Infrastructure

V5 introduces an `infra/aws` directory with Terraform configuration.

The infrastructure is split into two areas:

```text
infra/aws/
├── bootstrap/
└── stack/
```

The `bootstrap` directory is reserved for foundational resources such as remote state and CI/CD identity.

The `stack` directory contains the main IncidentHub AWS infrastructure.

Current stack files include:

```text
alb.tf
cloudwatch.tf
ecr.tf
ecs.tf
ecs-api.tf
ecs-worker.tf
iam-ecs.tf
network.tf
rds.tf
security-groups.tf
sns.tf
sqs.tf
variables.tf
outputs.tf
```

This keeps the infrastructure modular and easy to evolve.

---

### AWS Network Foundation

V5 creates a dedicated VPC for IncidentHub.

The network includes:

- VPC
- public subnets
- private subnets
- Internet Gateway
- public route table
- private route table
- route table associations

The validated development deployment uses:

```text
enable_nat_gateway = false
```

This avoids NAT Gateway costs during development.

For this low-cost setup, ECS tasks run with public IP assignment while RDS remains private behind security groups.

---

### Security Groups

V5 defines separate security groups for each responsibility.

```text
ALB security group
API security group
Worker security group
Database security group
```

The security model is:

```text
Internet → ALB:80
ALB → API:8080
API → RDS:5432
Worker → RDS:5432
```

The Worker does not require inbound traffic.

RDS is not publicly accessible and accepts PostgreSQL traffic only from the API and Worker security groups.

---

### ECR Repositories

V5 creates two ECR repositories:

```text
incidenthub-dev-api
incidenthub-dev-worker
```

These repositories store the Docker images used by ECS Fargate.

Both repositories include:

- image scanning on push
- AES256 encryption
- lifecycle policy for tagged images
- lifecycle policy for untagged images

The validated deployment used the image tag:

```text
dev
```

---

### ECS Fargate Runtime

V5 deploys IncidentHub as two ECS Fargate services:

```text
incidenthub-dev-api
incidenthub-dev-worker
```

Both services run in:

```text
incidenthub-dev-cluster
```

The API service is connected to the Application Load Balancer.

The Worker service runs independently with no public ingress.

Both use the Spring profile:

```text
cloud
```

---

### RDS PostgreSQL

V5 creates a managed PostgreSQL database using Amazon RDS.

The database stores:

- signals
- signal processing tasks
- rules
- incidents
- evidence
- timeline events
- alerts

The validated deployment used:

```text
database name: incidenthub
username: incidenthub
engine: PostgreSQL
publicly accessible: false
storage encrypted: true
```

The database password is managed by AWS rather than stored in Terraform variables.

For development cost control, automated backups were disabled:

```text
database_backup_retention_days = 0
```

---

### Application Load Balancer

V5 exposes the API through a public Application Load Balancer.

The ALB forwards traffic to the ECS API task.

Health check path:

```text
/actuator/health
```

The target group was validated as:

```text
healthy
```

This confirms that AWS can route external HTTP traffic to the API service.

---

### CloudWatch Logs and Alarms

V5 creates CloudWatch log groups for both runtime services:

```text
/ecs/incidenthub-dev/api
/ecs/incidenthub-dev/worker
```

CloudWatch logs were validated for:

- API startup
- Worker startup
- cloud Spring profile
- RDS connection
- Flyway migration execution
- application runtime logs

V5 also defines alarms for:

- ALB 5XX errors
- API unhealthy targets
- RDS high CPU
- RDS low free storage
- SQS backlog
- SQS oldest message age
- DLQ messages

---

### SQS and SNS Foundations

V5 creates cloud messaging foundations:

```text
incidenthub-dev-signal-processing
incidenthub-dev-signal-processing-dlq
incidenthub-dev-alerts
```

Current runtime behavior still uses the PostgreSQL-backed processing task model.

SQS and SNS are included to prepare for future cloud-native decoupling.

Current meaning:

```text
SQS → future signal processing queue
SNS → future alert delivery channel
```

---

### AWS Deployment Validation Script

V5 adds a validation script:

```text
scripts/aws/validate-aws-deployment.ps1
```

The script generates a Markdown report with checks for:

- AWS identity
- ECR API image
- ECR Worker image
- ECS service state
- ECS events
- ECS running tasks
- ALB details
- target group details
- target health
- API health through ALB
- operational metrics through ALB
- SLO summary through ALB
- Prometheus endpoint through ALB
- CloudWatch API logs
- CloudWatch Worker logs
- CloudWatch alarm states
- SQS queues
- SNS topics

This makes the AWS deployment repeatably verifiable.

---

## High-Level AWS Architecture

```text
                           ┌───────────────────────────┐
                           │        Internet           │
                           └─────────────┬─────────────┘
                                         │
                                         v
                           ┌───────────────────────────┐
                           │ Application Load Balancer │
                           └─────────────┬─────────────┘
                                         │
                                         v
                           ┌───────────────────────────┐
                           │ IncidentHub API           │
                           │ ECS Fargate               │
                           └─────────────┬─────────────┘
                                         │
                                         v
                           ┌───────────────────────────┐
                           │ RDS PostgreSQL            │
                           └─────────────▲─────────────┘
                                         │
                                         │
                           ┌─────────────┴─────────────┐
                           │ IncidentHub Worker        │
                           │ ECS Fargate               │
                           └───────────────────────────┘
```

Supporting services:

```text
ECR        Docker image registry
CloudWatch Logs and alarms
SQS        signal queue foundation
SNS        alert topic foundation
IAM        ECS execution and runtime roles
```

---

## Deployment Strategy

V5 uses a two-phase deployment strategy.

This avoids ECS trying to start containers before the ECR images exist.

### Phase 1

Create AWS infrastructure with services stopped:

```text
api_desired_count = 0
worker_desired_count = 0
```

This creates:

- VPC
- subnets
- security groups
- ECR repositories
- RDS
- ECS cluster
- ECS services
- ALB
- CloudWatch resources
- SQS
- SNS
- IAM roles

### Phase 2

Push Docker images to ECR:

```text
incidenthub-dev-api:dev
incidenthub-dev-worker:dev
```

### Phase 3

Start ECS services:

```text
api_desired_count = 1
worker_desired_count = 1
```

Expected result:

```text
API service running 1/1
Worker service running 1/1
```

---

## Manual Deployment Flow

### 1. Create local Terraform variables

Create:

```text
infra/aws/stack/terraform.tfvars
```

Example:

```hcl
aws_region   = "eu-west-1"
project_name = "incidenthub"
environment  = "dev"
owner        = "alberto"

api_desired_count    = 0
worker_desired_count = 0

enable_nat_gateway = false
database_backup_retention_days = 0
```

This file should not be committed.

---

### 2. Apply Terraform infrastructure

```powershell
cd infra\aws\stack

terraform init
terraform fmt
terraform validate
terraform plan -out=tfplan
terraform apply tfplan
```

Expected result:

```text
AWS infrastructure exists
ECR repositories exist
ECS services exist with desired count 0
```

---

### 3. Push Docker images to ECR

From the project root:

```powershell
.\scripts\aws\push-images-to-ecr.ps1 `
  -AwsRegion eu-west-1 `
  -ProjectName incidenthub `
  -Environment dev `
  -ImageTag dev
```

Expected images:

```text
<aws-account-id>.dkr.ecr.eu-west-1.amazonaws.com/incidenthub-dev-api:dev
<aws-account-id>.dkr.ecr.eu-west-1.amazonaws.com/incidenthub-dev-worker:dev
```

---

### 4. Start ECS services

Update `terraform.tfvars`:

```hcl
api_desired_count    = 1
worker_desired_count = 1
```

Then:

```powershell
cd infra\aws\stack

terraform plan -out=tfplan
terraform apply tfplan
```

Expected ECS state:

```text
incidenthub-dev-api     desired 1 / running 1
incidenthub-dev-worker  desired 1 / running 1
```

---

## Cloud Runtime Configuration

The API and Worker run with:

```text
SPRING_PROFILES_ACTIVE=cloud
```

Common runtime variables:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
LOGGING_LEVEL_ROOT
LOGGING_LEVEL_INCIDENTHUB
```

Worker-specific runtime variables:

```text
INCIDENTHUB_WORKER_ID
INCIDENTHUB_WORKER_BATCH_SIZE
INCIDENTHUB_WORKER_POLL_DELAY_MS
INCIDENTHUB_ALERTS_BATCH_SIZE
INCIDENTHUB_ALERTS_POLL_DELAY_MS
```

The datasource password is injected from the AWS-managed RDS master user secret.

---

## AWS Validation Results

The AWS deployment was validated with the validation script and direct API calls.

Validated AWS state:

```text
ECR API image ACTIVE
ECR Worker image ACTIVE
ECS API service ACTIVE
ECS Worker service ACTIVE
API desired 1 / running 1
Worker desired 1 / running 1
ALB active
Target Group healthy
API health UP
RDS connected
Flyway migrations applied
CloudWatch logs active
Operational metrics endpoint active
Prometheus endpoint active
SLO endpoint active
```

The API health endpoint returned:

```json
{
  "status": "UP",
  "groups": [
    "liveness",
    "readiness"
  ]
}
```

---

## End-to-End Functional Validation

After the AWS deployment was running, real signals were sent through the public ALB.

Validated incident scenarios:

```text
HIGH_LATENCY
HIGH_ERROR_RATE
TIMEOUT_SPIKE
```

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
Timeout Spike / Dependency Failure Runbook
        ↓
LOG alert sent
```

Final validated incident state:

```text
3 open incidents in AWS
```

---

## Example AWS Incident Result

Example incident list:

```text
TIMEOUT_SPIKE
severity: HIGH
status: OPEN
matched: 3 TIMEOUT signals within 60 seconds

HIGH_ERROR_RATE
severity: HIGH
status: OPEN
matched: 5 ERROR signals within 60 seconds

HIGH_LATENCY
severity: MEDIUM
status: OPEN
matched: repeated LATENCY signals
```

The system correctly deduplicated repeated latency signals into an existing `HIGH_LATENCY` incident by increasing its occurrence count rather than opening a duplicate incident.

---

## Example Incident Timeline

The validated incident timeline included:

```text
INCIDENT_OPENED
RUNBOOK_ATTACHED
ALERT_REQUESTED
EVIDENCE_ATTACHED
EVIDENCE_ATTACHED
EVIDENCE_ATTACHED
ALERT_SENT
```

This confirms that the AWS Worker executed the full incident response workflow.

---

## Final Operational Metrics

After the AWS functional validation:

```text
SIGNAL_PROCESSING_TASKS_TOTAL        14
SIGNAL_PROCESSING_TASKS_PROCESSED    14
SIGNAL_PROCESSING_TASKS_FAILED       0
SIGNAL_PROCESSING_SUCCESS_RATE       100%

INCIDENTS_OPEN                       3
INCIDENTS_ACTIVE                     3
INCIDENTS_HIGH_OPEN                  2

ALERTS_TOTAL                         3
ALERTS_SENT                          3
ALERTS_FAILED                        0
ALERT_DELIVERY_SUCCESS_RATE          100%
```

SLOs remained healthy:

```text
signal-processing-success-rate        HEALTHY
alert-delivery-success-rate           HEALTHY
pending-task-backlog                  HEALTHY
oldest-pending-task-age               HEALTHY
signal-processing-average-latency     HEALTHY
```

---

## API Endpoints Validated in AWS

### Health

```http
GET /actuator/health
```

### Incidents

```http
GET /incidents?limit=10
```

### Evidence

```http
GET /incidents/{incidentId}/evidence
```

### Timeline

```http
GET /incidents/{incidentId}/timeline
```

### Alerts

```http
GET /incidents/{incidentId}/alerts
```

### Operational Metrics

```http
GET /metrics/operational
```

### SLO Summary

```http
GET /slo/summary
```

### Prometheus Metrics

```http
GET /metrics/prometheus
```

---

## Useful AWS Commands

### ECS service summary

```powershell
aws ecs describe-services `
  --cluster incidenthub-dev-cluster `
  --services incidenthub-dev-api incidenthub-dev-worker `
  --region eu-west-1 `
  --query "services[*].{name:serviceName,desired:desiredCount,running:runningCount,pending:pendingCount,status:status}"
```

### Target health

```powershell
aws elbv2 describe-target-health `
  --target-group-arn <target-group-arn> `
  --region eu-west-1
```

### API logs

```powershell
aws logs tail "/ecs/incidenthub-dev/api" `
  --region eu-west-1 `
  --since 30m
```

### Worker logs

```powershell
aws logs tail "/ecs/incidenthub-dev/worker" `
  --region eu-west-1 `
  --since 30m
```

---

## Screenshot Checklist

Recommended screenshots for the portfolio:

- ECS cluster overview
- ECS API service running 1/1
- ECS Worker service running 1/1
- ECS running tasks
- ALB active
- Target Group healthy
- RDS PostgreSQL available
- ECR API image with `dev` tag
- ECR Worker image with `dev` tag
- CloudWatch API logs
- CloudWatch Worker logs
- CloudWatch alarms
- CloudWatch ALB metrics chart
- CloudWatch ECS CPU and memory charts
- `/actuator/health` response
- `/incidents?limit=10` showing AWS incidents
- `/metrics/operational` showing processed tasks and sent alerts
- `/slo/summary` showing healthy SLOs
- incident timeline showing runbook, evidence and alert sent

---

## Cost Control

For short demos, API and Worker can be stopped without destroying the whole stack:

```powershell
aws ecs update-service `
  --cluster incidenthub-dev-cluster `
  --service incidenthub-dev-api `
  --desired-count 0 `
  --region eu-west-1

aws ecs update-service `
  --cluster incidenthub-dev-cluster `
  --service incidenthub-dev-worker `
  --desired-count 0 `
  --region eu-west-1
```

This stops Fargate compute usage, but it does not stop all costs.

Resources such as RDS and ALB may still generate cost.

To fully remove the AWS deployment:

```powershell
cd infra\aws\stack
terraform destroy
```

---

## Current Status

### Version 5 complete

Implemented features:

- Terraform AWS stack skeleton
- ECR repositories for API and Worker
- Docker image publishing script
- VPC, subnets and routing
- security groups for ALB, API, Worker and database
- RDS PostgreSQL foundation
- ECS cluster
- CloudWatch log groups
- ECS task execution role
- ECS runtime task role
- Application Load Balancer
- API ECS task definition and service
- Worker ECS task definition and service
- SQS signal processing queue foundation
- SQS dead-letter queue foundation
- SNS alert topic foundation
- CloudWatch runtime health alarms
- AWS deployment validation script
- manual AWS deployment
- end-to-end incident detection validation in AWS

---

## Known Limitations

V5 intentionally does not include:

- GitHub Actions CI/CD deployment
- remote Terraform state
- Terraform state locking
- HTTPS listener and custom domain
- AWS ACM certificate
- NAT Gateway production networking
- private-only ECS tasks with VPC endpoints
- SNS alert sender implementation
- SQS runtime processing implementation
- CloudWatch custom metrics for IncidentHub internal metrics
- CloudWatch dashboard managed by Terraform
- Amazon Managed Prometheus
- Amazon Managed Grafana
- autoscaling policies
- WAF
- blue/green deployments
- multi-environment promotion flow

These belong to later versions.

---

## Next Version

### V6 – Advanced Operational Scenarios

Potential V6 focus:

- GitHub Actions CI/CD with OIDC
- remote Terraform backend
- CloudWatch dashboard
- CloudWatch custom metrics
- OpenTelemetry trace correlation
- synthetic checks
- heartbeat monitoring
- alert suppression windows
- escalation policies
- basic dashboard UI
- optional SNS alert delivery implementation

---

V5 is the version where IncidentHub becomes a real cloud-deployed platform.

It proves that the architecture can run in AWS, process operational signals through a public API, evaluate incidents asynchronously through a Worker, persist state in managed PostgreSQL, and expose health, metrics, SLOs, logs and alarms in a cloud environment.
