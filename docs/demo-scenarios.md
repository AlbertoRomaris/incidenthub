# IncidentHub – Demo Scenarios

This document contains practical demo scenarios for validating and showcasing IncidentHub.

The scenarios are designed for local and AWS deployments.

---

## Prerequisites

For local demos:

```text
Docker
Docker Compose
Java 21
API running
Worker running
PostgreSQL running
```

For AWS demos:

```text
AWS CLI configured
Terraform stack applied
API and Worker deployed on ECS
ALB DNS available
SNS topic available if testing SNS alerts
```

Set the API base URL.

Local example:

```powershell
$baseUrl = "http://localhost:8081"
```

AWS example:

```powershell
$baseUrl = "http://<alb-dns-name>"
```

---

## Scenario 1 – Health Check

### Goal

Confirm that the API is reachable.

### Command

```powershell
Invoke-RestMethod -Uri "$baseUrl/actuator/health"
```

### Expected Result

```text
status = UP
```

---

## Scenario 2 – Operational Metrics

### Goal

Confirm that operational metrics are exposed.

### Command

```powershell
(Invoke-RestMethod -Uri "$baseUrl/metrics/operational").metrics |
  Format-Table name,type,unit,value
```

### Expected Result

Metrics such as:

```text
SIGNAL_PROCESSING_TASKS_TOTAL
SIGNAL_PROCESSING_TASKS_PENDING
SIGNAL_PROCESSING_TASKS_PROCESSED
SIGNAL_PROCESSING_TASKS_FAILED
SIGNAL_PROCESSING_SUCCESS_RATE
SIGNAL_PROCESSING_AVERAGE_LATENCY_MS
INCIDENTS_OPEN
INCIDENTS_ACTIVE
ALERTS_TOTAL
ALERTS_SENT
ALERT_DELIVERY_SUCCESS_RATE
```

---

## Scenario 3 – SLO Summary

### Goal

Confirm that SLOs are calculated and exposed.

### Command

```powershell
(Invoke-RestMethod -Uri "$baseUrl/slo/summary").slos |
  Format-Table key,actualValue,targetValue,unit,status
```

### Expected Result

SLOs such as:

```text
signal-processing-success-rate
alert-delivery-success-rate
pending-task-backlog
oldest-pending-task-age
signal-processing-average-latency
```

Expected status in a healthy demo:

```text
HEALTHY
```

---

## Scenario 4 – Create a HIGH_LATENCY Incident

### Goal

Send enough latency signals to trigger a `HIGH_LATENCY` incident.

### Command

```powershell
$testEnv = "demo-latency-" + (Get-Date -Format "HHmmss")
```

```powershell
1..3 | ForEach-Object {
  Invoke-RestMethod `
    -Uri "$baseUrl/signals" `
    -Method Post `
    -ContentType "application/json" `
    -Body "{
      `"serviceName`": `"payment-service`",
      `"environment`": `"$testEnv`",
      `"signalType`": `"LATENCY`",
      `"severity`": `"MEDIUM`",
      `"message`": `"High payment latency detected`",
      `"correlationId`": `"demo-latency-00$_`",
      `"latencyMs`": 2500,
      `"statusCode`": 200,
      `"errorCode`": `"PAYMENT_HIGH_LATENCY`",
      `"attributes`": {
        `"source`": `"demo-scenario`"
      }
    }"
}
```

Wait 15-30 seconds for the Worker to process the tasks.

### Check Incident

```powershell
$incident = (Invoke-RestMethod -Uri "$baseUrl/incidents?limit=20") |
  Where-Object { $_.environment -eq $testEnv } |
  Select-Object -First 1

$incident
```

### Expected Result

```text
incidentType = HIGH_LATENCY
status       = OPEN
severity     = MEDIUM
serviceName  = payment-service
environment  = demo-latency-...
```

---

## Scenario 5 – Check Evidence

### Goal

Confirm that the incident has evidence attached.

### Command

```powershell
Invoke-RestMethod -Uri "$baseUrl/incidents/$($incident.incidentId)/evidence"
```

### Expected Result

Evidence entries linked to the signals that caused the incident.

Expected fields:

```text
evidenceId
incidentId
signalId
ruleId
capturedAt
summary
attributes
```

Expected summary:

```text
Signal matched rule: High latency on payment-service
```

---

## Scenario 6 – Check Timeline

### Goal

Confirm that the incident lifecycle is recorded.

### Command

```powershell
Invoke-RestMethod -Uri "$baseUrl/incidents/$($incident.incidentId)/timeline" |
  Format-Table eventType,summary,actor,occurredAt
```

### Expected Result

Timeline events such as:

```text
INCIDENT_OPENED
RUNBOOK_ATTACHED
ALERT_REQUESTED
EVIDENCE_ATTACHED
ALERT_SENT
```

---

## Scenario 7 – Check Alerts

### Goal

Confirm that an alert was created and sent.

### Command

```powershell
Invoke-RestMethod -Uri "$baseUrl/incidents/$($incident.incidentId)/alerts" |
  Format-Table channel,status,title,sentAt,failedAt,failureReason
```

### Expected Local Result

```text
channel = LOG
status  = SENT
```

### Expected AWS Result

```text
channel = SNS
status  = SENT
```

---

## Scenario 8 – Create a HIGH_ERROR_RATE Incident

### Goal

Send repeated error signals to trigger a `HIGH_ERROR_RATE` incident.

### Command

```powershell
$testEnv = "demo-errors-" + (Get-Date -Format "HHmmss")
```

```powershell
1..5 | ForEach-Object {
  Invoke-RestMethod `
    -Uri "$baseUrl/signals" `
    -Method Post `
    -ContentType "application/json" `
    -Body "{
      `"serviceName`": `"payment-service`",
      `"environment`": `"$testEnv`",
      `"signalType`": `"ERROR`",
      `"severity`": `"HIGH`",
      `"message`": `"Payment service error spike detected`",
      `"correlationId`": `"demo-error-00$_`",
      `"latencyMs`": 120,
      `"statusCode`": 500,
      `"errorCode`": `"PAYMENT_ERROR`",
      `"attributes`": {
        `"source`": `"demo-scenario`"
      }
    }"
}
```

Wait 15-30 seconds.

### Check Incident

```powershell
$incident = (Invoke-RestMethod -Uri "$baseUrl/incidents?limit=20") |
  Where-Object { $_.environment -eq $testEnv } |
  Select-Object -First 1

$incident
```

### Expected Result

```text
incidentType = HIGH_ERROR_RATE
status       = OPEN
severity     = HIGH
```

---

## Scenario 9 – Create a TIMEOUT_SPIKE Incident

### Goal

Send repeated timeout signals to trigger a `TIMEOUT_SPIKE` incident.

### Command

```powershell
$testEnv = "demo-timeouts-" + (Get-Date -Format "HHmmss")
```

```powershell
1..3 | ForEach-Object {
  Invoke-RestMethod `
    -Uri "$baseUrl/signals" `
    -Method Post `
    -ContentType "application/json" `
    -Body "{
      `"serviceName`": `"payment-service`",
      `"environment`": `"$testEnv`",
      `"signalType`": `"TIMEOUT`",
      `"severity`": `"HIGH`",
      `"message`": `"Payment timeout detected`",
      `"correlationId`": `"demo-timeout-00$_`",
      `"latencyMs`": 10000,
      `"statusCode`": 504,
      `"errorCode`": `"PAYMENT_TIMEOUT`",
      `"attributes`": {
        `"source`": `"demo-scenario`"
      }
    }"
}
```

Wait 15-30 seconds.

### Expected Result

```text
incidentType = TIMEOUT_SPIKE
status       = OPEN
severity     = HIGH
```

---

## Scenario 10 – Deduplication

### Goal

Confirm that repeated matching signals update an active incident instead of creating duplicates.

### Steps

1. Create a `HIGH_LATENCY` incident using Scenario 4.
2. Reuse the same `environment` and `serviceName`.
3. Send three more matching LATENCY signals.

### Expected Result

The existing active incident should be updated:

```text
occurrenceCount increases
lastSeenAt changes
no duplicate active incident for same environment/service/type
```

---

## Scenario 11 – SNS Email Delivery in AWS

### Goal

Confirm that AWS SNS delivers alerts outside the application.

### Prerequisites

SNS topic exists:

```text
incidenthub-dev-alerts
```

Email subscription confirmed.

### Subscribe Email

```powershell
$topicArn = "arn:aws:sns:eu-west-1:<account-id>:incidenthub-dev-alerts"
```

```powershell
aws sns subscribe `
  --topic-arn $topicArn `
  --protocol email `
  --notification-endpoint "your-email@example.com" `
  --region eu-west-1
```

Confirm the subscription from the email sent by AWS.

### Trigger Incident

Use Scenario 4 with a new environment.

### Expected Result

Alert endpoint:

```text
channel = SNS
status  = SENT
```

Timeline:

```text
ALERT_SENT
```

External result:

```text
SNS alert email received
```

---

## Scenario 12 – GitHub Actions CI

### Goal

Confirm that the project builds automatically on GitHub.

### Steps

1. Push a commit to `main`.
2. Open GitHub.
3. Go to `Actions`.
4. Open `CI`.

### Expected Result

```text
Build and test = Success
```

---

## Scenario 13 – GitHub Actions Docker Publish to ECR

### Goal

Confirm that GitHub Actions publishes Docker images to ECR.

### Steps

1. Push a commit affecting backend, worker, infrastructure or workflow files.
2. Open GitHub Actions.
3. Open `Publish Docker Images`.

### Expected Result

Workflow completes successfully.

Check ECR:

```powershell
aws ecr describe-images `
  --repository-name incidenthub-dev-api `
  --image-ids imageTag=dev `
  --region eu-west-1 `
  --query "imageDetails[0].{tags:imageTags,pushedAt:imagePushedAt,digest:imageDigest,size:imageSizeInBytes}"
```

```powershell
aws ecr describe-images `
  --repository-name incidenthub-dev-worker `
  --image-ids imageTag=dev `
  --region eu-west-1 `
  --query "imageDetails[0].{tags:imageTags,pushedAt:imagePushedAt,digest:imageDigest,size:imageSizeInBytes}"
```

Expected:

```text
Fresh pushedAt timestamp
```

---

## Scenario 14 – GitHub Actions ECS Redeploy

### Goal

Confirm that GitHub Actions redeploys ECS services after publishing images.

### Command

```powershell
aws ecs describe-services `
  --cluster incidenthub-dev-cluster `
  --services incidenthub-dev-api incidenthub-dev-worker `
  --region eu-west-1 `
  --query "services[*].{name:serviceName,desired:desiredCount,running:runningCount,pending:pendingCount,status:status,taskDefinition:taskDefinition}"
```

### Expected Result

```text
desired = 1
running = 1
pending = 0
```

The GitHub Actions workflow should also wait until services are stable.

---

## Scenario 15 – CloudWatch Dashboard Review

### Goal

Show runtime observability in AWS.

### AWS Console

Go to:

```text
CloudWatch → Dashboards → incidenthub-dev-runtime-dashboard
```

### Expected Panels

- ALB requests
- ALB 2XX/5XX
- ALB target response time
- ECS API CPU/memory
- ECS Worker CPU/memory
- RDS CPU/connections
- RDS free storage
- SQS backlog
- DLQ messages

---

## Recommended Demo Flow for Portfolio

Best order for a live walkthrough:

```text
1. Show README and architecture docs
2. Start local Docker Compose or open AWS deployment
3. Show health endpoint
4. Show metrics and SLOs
5. Send LATENCY signals
6. Show incident created
7. Show evidence
8. Show timeline
9. Show alert
10. Show SNS email if using AWS
11. Show CloudWatch dashboard
12. Show GitHub Actions CI/CD workflow
13. Show Terraform infrastructure
```

---

## Screenshot Checklist

Useful screenshots:

- README project overview
- Architecture diagram
- GitHub Actions CI success
- GitHub Actions publish workflow success
- ECR images with fresh `dev` timestamp
- ECS services running
- API health response
- incident list
- evidence response
- timeline response
- alert response with `SNS` and `SENT`
- SNS email received
- CloudWatch runtime dashboard
