# V6 – Delivery Automation & Advanced Cloud Operations

## Overview

IncidentHub V6 evolves the platform from a manually deployed AWS system into a more automated and operationally mature cloud platform.

V5 proved that IncidentHub could run in AWS using Terraform, ECR, ECS Fargate, RDS PostgreSQL, an Application Load Balancer, CloudWatch logs, CloudWatch alarms, SQS foundations and SNS foundations.

V6 builds on top of that cloud deployment and adds the operational delivery layer:

- CloudWatch runtime dashboard
- real SNS alert delivery
- email delivery validation through SNS subscription
- GitHub Actions CI workflow
- GitHub Actions OIDC integration with AWS
- GitHub Actions Docker image publishing to ECR
- GitHub Actions ECS redeployment after publishing images
- improved ECR publishing script behavior
- stronger end-to-end cloud validation after automated deployment

The goal of V6 is to make IncidentHub feel closer to a real production-style engineering project: observable, automated, repeatable and externally integrated.

---

## Why V6 Exists

V5 could answer cloud runtime questions:

```text
Can IncidentHub run in AWS?
Can the API receive traffic through an ALB?
Can the Worker process signals in ECS?
Can the system persist state in RDS?
Can incidents, evidence, timelines, runbooks, alerts, metrics and SLOs work in AWS?
```

V6 starts answering delivery and operational maturity questions:

```text
Can the platform be validated automatically on every push?
Can Docker images be built and pushed without manual local commands?
Can GitHub Actions authenticate to AWS without storing long-lived credentials?
Can ECS services be redeployed automatically after publishing new images?
Can runtime health be visualized in an AWS dashboard?
Can alerts be delivered through a real cloud notification service?
Can SNS deliver IncidentHub alerts to an external subscriber?
```

This moves IncidentHub from:

```text
cloud-deployed operational platform
```

to:

```text
cloud-deployed, observable and delivery-automated operational platform
```

The key idea of V6 is:

```text
A cloud deployment is stronger when deployment, validation, observability and alert delivery are repeatable.
```

---

## High-Level Flow

```text
Developer pushes to main
        ↓
GitHub Actions CI
        ↓
Maven build and test
        ↓
GitHub Actions assumes AWS IAM role using OIDC
        ↓
Docker images are built
        ↓
Images are pushed to Amazon ECR
        ↓
ECS API and Worker services are redeployed
        ↓
AWS runtime remains observable through CloudWatch Dashboard
        ↓
IncidentHub processes cloud signals
        ↓
Worker publishes real alerts to SNS
        ↓
SNS delivers alert email to confirmed subscriber
```

---

## Main V6 Capabilities

### CloudWatch Runtime Dashboard

V6 adds a Terraform-managed CloudWatch dashboard for IncidentHub runtime visibility.

The dashboard provides a single AWS screen for the most important cloud runtime signals:

- ALB request count
- ALB target 2XX responses
- ALB 5XX errors
- ALB target health
- ALB target response time
- ECS API CPU and memory
- ECS Worker CPU and memory
- RDS CPU utilization
- RDS database connections
- RDS free storage
- SQS signal queue visible messages
- SQS oldest message age
- DLQ visible messages

Dashboard name:

```text
incidenthub-dev-runtime-dashboard
```

This makes the AWS deployment easier to review visually and provides useful screenshots for portfolio documentation.

---

### SNS Alert Delivery

V6 replaces the cloud alert delivery path from log-only alerting to real AWS SNS publishing.

Previous behavior:

```text
Incident opened
        ↓
Alert created with channel LOG
        ↓
Worker writes alert to logs
        ↓
Alert marked SENT
```

V6 cloud behavior:

```text
Incident opened
        ↓
Alert created with channel SNS
        ↓
Worker publishes message to SNS topic
        ↓
SNS accepts publish request
        ↓
Alert marked SENT
```

SNS topic:

```text
incidenthub-dev-alerts
```

The existing alert model already supported multiple channels, including SNS. V6 adds the runtime implementation that actually publishes to AWS SNS.

---

### Configurable Alert Channel

V6 makes the alert channel configurable instead of hardcoding `LOG` inside the signal processing use case.

Local/default behavior:

```text
incidenthub.alerts.channel=LOG
incidenthub.alerts.sender=log
```

AWS ECS Worker behavior:

```text
incidenthub.alerts.channel=SNS
incidenthub.alerts.sender=sns
incidenthub.alerts.sns.topic-arn=<sns-topic-arn>
```

This keeps local development simple while enabling real cloud alert delivery in AWS.

---

### SNS Email Delivery Validation

SNS was validated beyond the application boundary.

A real email subscription was created for the SNS topic:

```text
protocol: email
status: confirmed
```

After a new IncidentHub incident was created in AWS, the Worker published the alert to SNS and SNS delivered it to the confirmed email subscriber.

Validated flow:

```text
IncidentHub Worker on ECS
        ↓
AWS SNS Publish
        ↓
SNS Topic
        ↓
Confirmed email subscription
        ↓
Alert email received
```

This proves that the alert path is not only internally marked as `SENT`, but also reaches an external notification channel.

---

### Improved ECR Publishing Script

V6 improves the local ECR publishing script:

```text
scripts/aws/push-images-to-ecr.ps1
```

Previous problem:

```text
Docker login or Docker build could fail
        ↓
Script continued anyway
        ↓
Old local images could be pushed
        ↓
Script still printed Done
```

V6 behavior:

```text
Docker login fails       → script stops
Repository check fails   → script stops
Docker build fails       → script stops
Docker push fails        → script stops
Only successful flow     → script prints Done
```

The script now uses stricter error handling and checks native command exit codes.

This makes local deployment tooling safer and avoids silently publishing stale images.

---

### GitHub Actions CI

V6 adds a basic GitHub Actions CI workflow:

```text
.github/workflows/ci.yml
```

The CI workflow runs on:

```text
push to main
pull request to main
```

It performs:

```text
checkout repository
set up Java 21
restore Maven cache
mvn clean package
```

This validates the Java multi-module project automatically.

The CI workflow confirms that the codebase builds successfully outside the developer machine.

---

### GitHub Actions OIDC Integration with AWS

V6 adds a Terraform-managed AWS IAM role for GitHub Actions.

Role:

```text
incidenthub-dev-github-actions-deployer
```

The role is assumed by GitHub Actions using OpenID Connect.

This avoids long-lived AWS access keys in GitHub secrets.

Trust policy scope:

```text
repo:AlbertoRomaris/incidenthub:ref:refs/heads/main
```

That means only workflows running from the `main` branch of the IncidentHub repository can assume the deployment role.

OIDC provider:

```text
https://token.actions.githubusercontent.com
```

Audience:

```text
sts.amazonaws.com
```

This is closer to real-world CI/CD security than storing static AWS credentials.

---

### GitHub Actions Docker Publishing to ECR

V6 adds a GitHub Actions workflow that publishes Docker images to ECR:

```text
.github/workflows/publish-images.yml
```

The workflow builds and publishes:

```text
incidenthub-dev-api:dev
incidenthub-dev-worker:dev
```

Target repositories:

```text
incidenthub-dev-api
incidenthub-dev-worker
```

High-level workflow:

```text
checkout repository
        ↓
assume AWS role through OIDC
        ↓
login to Amazon ECR
        ↓
build API Docker image
        ↓
push API image to ECR
        ↓
build Worker Docker image
        ↓
push Worker image to ECR
```

This replaces the need to manually run the image publishing script for normal `main` branch changes.

---

### GitHub Actions ECS Redeployment

V6 extends the Docker publishing workflow so that, after new images are published, GitHub Actions redeploys ECS services.

The workflow forces a new deployment for:

```text
incidenthub-dev-api
incidenthub-dev-worker
```

The deployment step runs:

```text
aws ecs update-service --force-new-deployment
```

for both services.

Then it waits until both ECS services are stable:

```text
aws ecs wait services-stable
```

This gives IncidentHub a simple CI/CD path:

```text
Push to main
        ↓
Build images
        ↓
Push to ECR
        ↓
Redeploy ECS API and Worker
        ↓
Wait until stable
```

---

## High-Level V6 Architecture

```text
Developer
   |
   v
GitHub Repository
   |
   v
GitHub Actions
   |
   |-- CI: Maven build
   |
   |-- OIDC: assume AWS IAM role
   |
   |-- Docker build API
   |
   |-- Docker build Worker
   |
   |-- Push images to ECR
   |
   v
Amazon ECR
   |
   v
ECS Fargate Services
   |
   |-- IncidentHub API
   |-- IncidentHub Worker
   |
   v
Runtime AWS Platform
   |
   |-- ALB
   |-- RDS PostgreSQL
   |-- CloudWatch Dashboard
   |-- CloudWatch Logs
   |-- CloudWatch Alarms
   |-- SNS Alerts
```

---

## Alert Delivery Architecture

```text
Signal sent through ALB
        ↓
IncidentHub API
        ↓
RDS PostgreSQL
        ↓
IncidentHub Worker
        ↓
Rule Evaluation
        ↓
Incident Opened
        ↓
Alert Created with channel SNS
        ↓
SnsAlertSender
        ↓
AWS SNS Topic
        ↓
Confirmed Email Subscription
        ↓
Alert Email Received
```

---

## Delivery Automation Architecture

```text
Push to main
        ↓
GitHub Actions starts
        ↓
CI build validates code
        ↓
OIDC token requested
        ↓
AWS IAM role assumed
        ↓
Docker images built
        ↓
Images pushed to ECR
        ↓
ECS services forced to redeploy
        ↓
Services wait until stable
        ↓
Cloud runtime updated
```

---

## AWS Infrastructure Added or Extended in V6

### CloudWatch Dashboard

Terraform resource:

```text
aws_cloudwatch_dashboard.main
```

Dashboard:

```text
incidenthub-dev-runtime-dashboard
```

The dashboard visualizes runtime health and infrastructure behavior across ALB, ECS, RDS and SQS.

---

### GitHub OIDC Provider

Terraform resource:

```text
aws_iam_openid_connect_provider.github
```

Provider URL:

```text
https://token.actions.githubusercontent.com
```

Client ID:

```text
sts.amazonaws.com
```

This enables GitHub Actions to request short-lived AWS credentials.

---

### GitHub Actions Deployer Role

Terraform resource:

```text
aws_iam_role.github_actions_deployer
```

Role:

```text
incidenthub-dev-github-actions-deployer
```

This role is trusted only by the configured GitHub repository and branch.

---

### ECR Push Policy

Terraform policy:

```text
incidenthub-dev-github-actions-ecr-push-policy
```

Allows GitHub Actions to push images to the IncidentHub API and Worker repositories.

---

### ECS Deploy Policy

Terraform policy:

```text
incidenthub-dev-github-actions-ecs-deploy-policy
```

Allows GitHub Actions to describe and redeploy the IncidentHub API and Worker ECS services.

This is enough to force a new deployment after publishing images.

---

## Application Changes in V6

### SNS Alert Sender

V6 adds a new alert sender implementation:

```text
SnsAlertSender
```

Responsibilities:

- validate that the alert channel is SNS
- build a readable SNS message
- publish the alert to the configured SNS topic
- rely on the AWS SDK SNS client for delivery

---

### Alert Sender Configuration

V6 adds configuration-driven sender selection.

Default local sender:

```text
log
```

Cloud sender:

```text
sns
```

This avoids creating multiple ambiguous `AlertSender` beans and keeps alert delivery behavior explicit.

---

### Process Signal Use Case Alert Channel

The signal processing use case no longer hardcodes:

```text
AlertChannel.LOG
```

Instead, it receives the alert channel from application configuration.

This keeps the core application independent from Spring and infrastructure details while making runtime behavior configurable.

---

### Worker Runtime Configuration

The Worker now supports:

```yaml
incidenthub:
  alerts:
    channel: ${INCIDENTHUB_ALERTS_CHANNEL:LOG}
    sender: ${INCIDENTHUB_ALERTS_SENDER:log}
    sns:
      topic-arn: ${INCIDENTHUB_ALERTS_SNS_TOPIC_ARN:}
```

In AWS ECS, Terraform sets:

```text
INCIDENTHUB_ALERTS_CHANNEL=SNS
INCIDENTHUB_ALERTS_SENDER=sns
INCIDENTHUB_ALERTS_SNS_TOPIC_ARN=<sns-topic-arn>
AWS_REGION=eu-west-1
AWS_DEFAULT_REGION=eu-west-1
```

---

## GitHub Actions Workflows

### CI Workflow

File:

```text
.github/workflows/ci.yml
```

Purpose:

```text
Validate the Java project on push and pull request.
```

Main steps:

```text
checkout
setup Java 21
mvn clean package
```

---

### Publish Images Workflow

File:

```text
.github/workflows/publish-images.yml
```

Purpose:

```text
Build and publish API and Worker Docker images to ECR, then redeploy ECS services.
```

Main steps:

```text
checkout
configure AWS credentials through OIDC
login to ECR
build and push API image
build and push Worker image
force ECS new deployment
wait for ECS services stable
```

Required permissions:

```yaml
permissions:
  contents: read
  id-token: write
```

`id-token: write` is required so GitHub Actions can request an OIDC token and assume the AWS IAM role.

---

## Validation Results

### CloudWatch Dashboard Validation

The dashboard was created successfully and displayed runtime metrics for:

```text
Worker CPU
RDS CPU
RDS database connections
RDS free storage
SQS and DLQ metrics
```

The SQS and DLQ panels are expected to remain mostly flat for now because the current runtime still uses the PostgreSQL-backed processing task model.

Current behavior:

```text
signals → RDS → signal_processing_tasks → Worker
```

Future queue-based behavior:

```text
signals → SQS → Worker → RDS/incidents
```

---

### SNS Alert Delivery Validation

A new environment was used to avoid incident deduplication.

Three LATENCY signals were sent through the ALB.

Expected rule behavior:

```text
3 LATENCY signals within 60 seconds
        ↓
HIGH_LATENCY incident
```

Validated alert result:

```text
channel = SNS
status  = SENT
```

Validated timeline:

```text
INCIDENT_OPENED
RUNBOOK_ATTACHED
ALERT_REQUESTED
EVIDENCE_ATTACHED
EVIDENCE_ATTACHED
EVIDENCE_ATTACHED
ALERT_SENT
```

Timeline confirmed:

```text
Alert sent through channel: SNS
```

---

### SNS Email Validation

A real email subscription was added to the SNS topic.

Initial subscription status:

```text
pending confirmation
```

After confirming the subscription email, a new incident produced an SNS email notification.

Validated result:

```text
IncidentHub Worker ECS → SNS Topic → Email subscriber
```

This confirms external notification delivery.

---

### GitHub Actions CI Validation

The CI workflow ran successfully on GitHub.

Validated result:

```text
CI workflow: success
Build and test job: green
```

This confirms the project builds correctly in GitHub Actions.

---

### GitHub Actions ECR Publishing Validation

After pushing the workflow to `main`, GitHub Actions successfully published images to ECR.

Validated repositories:

```text
incidenthub-dev-api:dev
incidenthub-dev-worker:dev
```

ECR showed fresh `pushedAt` timestamps for both images.

This confirms:

```text
GitHub Actions → OIDC → AWS → ECR push
```

---

### GitHub Actions ECS Redeploy Validation

The workflow was extended to force a new ECS deployment after image publication.

Validated expected state:

```text
incidenthub-dev-api     desired 1 / running 1 / pending 0
incidenthub-dev-worker  desired 1 / running 1 / pending 0
```

The workflow waits until both services are stable before completing.

This confirms:

```text
GitHub Actions → ECR push → ECS redeploy → services stable
```

---

## Useful Validation Commands

### Check ECS Services

```powershell
aws ecs describe-services `
  --cluster incidenthub-dev-cluster `
  --services incidenthub-dev-api incidenthub-dev-worker `
  --region eu-west-1 `
  --query "services[*].{name:serviceName,desired:desiredCount,running:runningCount,pending:pendingCount,status:status,taskDefinition:taskDefinition}"
```

Expected:

```text
desired = 1
running = 1
pending = 0
```

---

### Check ECR Images

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

---

### Check API Health

```powershell
$alb = "http://<alb-dns-name>"

Invoke-RestMethod -Uri "$alb/actuator/health"
```

Expected:

```json
{
  "status": "UP"
}
```

---

### Send SNS Validation Signals

```powershell
$testEnv = "aws-sns-test-" + (Get-Date -Format "HHmmss")
```

```powershell
1..3 | ForEach-Object {
  Invoke-RestMethod `
    -Uri "$alb/signals" `
    -Method Post `
    -ContentType "application/json" `
    -Body "{
      `"serviceName`": `"payment-service`",
      `"environment`": `"$testEnv`",
      `"signalType`": `"LATENCY`",
      `"severity`": `"MEDIUM`",
      `"message`": `"SNS validation signal`",
      `"correlationId`": `"aws-sns-test-00$_`",
      `"latencyMs`": 2500,
      `"statusCode`": 200,
      `"errorCode`": `"PAYMENT_HIGH_LATENCY`",
      `"attributes`": {
        `"source`": `"sns-validation`"
      }
    }"
}
```

---

### Find the New Incident

```powershell
$incident = (Invoke-RestMethod -Uri "$alb/incidents?limit=20") |
  Where-Object { $_.environment -eq $testEnv } |
  Select-Object -First 1

$incident
```

---

### Check SNS Alert

```powershell
Invoke-RestMethod -Uri "$alb/incidents/$($incident.incidentId)/alerts" |
  Format-Table channel,status,title,sentAt,failedAt,failureReason
```

Expected:

```text
channel  status
-------  ------
SNS      SENT
```

---

### Check Timeline

```powershell
Invoke-RestMethod -Uri "$alb/incidents/$($incident.incidentId)/timeline" |
  Format-Table eventType,summary,actor,occurredAt
```

Expected:

```text
ALERT_REQUESTED
ALERT_SENT
```

---

### Check Worker Logs

```powershell
aws logs tail "/ecs/incidenthub-dev/worker" `
  --region eu-west-1 `
  --since 15m
```

---

### Subscribe Email to SNS Topic

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

Then confirm the email subscription from the AWS email.

Check subscription:

```powershell
aws sns list-subscriptions-by-topic `
  --topic-arn $topicArn `
  --region eu-west-1
```

---

## Screenshot Checklist

Recommended screenshots for V6 portfolio documentation:

- CloudWatch Runtime Dashboard
- CloudWatch Dashboard ALB metrics
- CloudWatch Dashboard ECS metrics
- CloudWatch Dashboard RDS metrics
- SNS topic
- SNS confirmed email subscription
- Received SNS alert email
- Incident alert endpoint showing `channel SNS` and `status SENT`
- Incident timeline showing `ALERT_SENT through channel SNS`
- GitHub Actions CI workflow green
- GitHub Actions Publish Docker Images workflow green
- GitHub Actions OIDC deployer role in IAM
- ECR API image with fresh `dev` push
- ECR Worker image with fresh `dev` push
- ECS API service running 1/1 after workflow
- ECS Worker service running 1/1 after workflow

---

## Cost Control

V6 does not remove the need to manage AWS costs carefully.

For short demos, API and Worker can be stopped:

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

Resources that may still generate cost:

```text
RDS PostgreSQL
Application Load Balancer
CloudWatch logs
CloudWatch alarms
SNS usage
ECR storage
```

To fully remove the AWS deployment:

```powershell
cd infra\aws\stack
terraform destroy
```

---

## Current Status

### Version 6 complete

Implemented features:

- CloudWatch runtime dashboard managed by Terraform
- dashboard widgets for ALB, ECS, RDS and SQS
- SNS alert sender implementation
- configurable alert channel
- configurable alert sender selection
- configurable SNS topic ARN
- ECS Worker configured for SNS alert delivery
- SNS alert delivery validated in AWS
- SNS email subscription validated
- improved ECR publishing script error handling
- GitHub Actions CI workflow
- GitHub Actions OIDC provider in AWS
- GitHub Actions IAM deployer role
- GitHub Actions ECR push permissions
- GitHub Actions ECS redeploy permissions
- GitHub Actions Docker image publishing workflow
- GitHub Actions ECS redeploy after image publishing
- workflow validation through ECR pushed images and ECS stable services

---

## Known Limitations

V6 intentionally does not include:

- remote Terraform backend with S3 and DynamoDB locking
- production NAT Gateway networking
- HTTPS listener
- custom domain
- ACM certificate
- WAF
- ECS autoscaling
- blue/green deployments
- image tags based on Git SHA
- immutable production image promotion
- separate dev/staging/prod environments
- CloudWatch custom metrics for IncidentHub internal application metrics
- Amazon Managed Prometheus
- Amazon Managed Grafana
- OpenTelemetry distributed tracing
- alert suppression windows
- escalation policies
- on-call schedules
- dashboard UI
- SQS runtime processing implementation

These can be added in later versions or treated as optional production-hardening extensions.

---

## Next Version

### V7 – Product UI, Production Hardening or Advanced SRE Scenarios

Potential V7 focus:

- basic web dashboard UI
- incident detail page
- timeline and evidence visualization
- SLO dashboard visualization
- remote Terraform backend
- Git SHA image tagging
- Terraform-managed GitHub environments
- HTTPS and custom domain
- CloudWatch custom metrics
- OpenTelemetry tracing
- heartbeat monitoring
- synthetic checks
- alert suppression and escalation policies
- SQS-based signal processing runtime

---

V6 is the version where IncidentHub starts behaving like a real cloud engineering project.

It connects code changes, automated validation, Docker image publishing, ECS redeployment, runtime observability and real AWS notification delivery.

IncidentHub is no longer only a local incident response system or a manually deployed AWS demo.

It is now a cloud-deployed platform with automated delivery and operational visibility.
