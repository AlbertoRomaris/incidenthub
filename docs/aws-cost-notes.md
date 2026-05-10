# IncidentHub – AWS Cost Notes

This document summarizes AWS cost considerations for the IncidentHub deployment.

IncidentHub is designed as a portfolio and learning project. AWS resources should be created only when needed for validation, demos or screenshots, and destroyed afterwards to avoid unnecessary cost.

---

## Main Cost-Producing Resources

The most relevant IncidentHub AWS resources from a cost perspective are:

- Amazon RDS PostgreSQL
- Application Load Balancer
- ECS Fargate API service
- ECS Fargate Worker service
- CloudWatch logs
- CloudWatch alarms
- ECR image storage
- SNS usage
- SQS usage
- NAT Gateway if ever introduced in future versions

The current cost-sensitive resources are mainly:

```text
RDS
ALB
ECS Fargate
CloudWatch
ECR
```

---

## ECS Fargate

IncidentHub runs two ECS services:

```text
incidenthub-dev-api
incidenthub-dev-worker
```

Each service runs as an ECS Fargate task.

Fargate costs depend on:

- number of running tasks
- configured CPU
- configured memory
- runtime duration

To reduce costs during idle periods, set desired count to zero:

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

Check status:

```powershell
aws ecs describe-services `
  --cluster incidenthub-dev-cluster `
  --services incidenthub-dev-api incidenthub-dev-worker `
  --region eu-west-1 `
  --query "services[*].{name:serviceName,desired:desiredCount,running:runningCount,pending:pendingCount,status:status}"
```

Expected stopped state:

```text
desired = 0
running = 0
pending = 0
```

---

## RDS PostgreSQL

RDS is usually one of the most important resources to control.

IncidentHub uses:

```text
Amazon RDS PostgreSQL
```

Cost depends on:

- instance class
- storage size
- storage type
- backup retention
- runtime duration
- snapshots

In development, the instance is intentionally small.

Important note:

```text
Stopping ECS does not stop RDS cost.
```

To fully stop cost from RDS, destroy the stack or delete the RDS instance.

---

## Application Load Balancer

The ALB provides public HTTP access to the API.

Cost depends on:

- ALB running time
- traffic
- load balancer capacity units

Important note:

```text
Stopping ECS tasks does not delete the ALB.
```

If the ALB remains provisioned, it can still generate cost.

To remove it, run Terraform destroy.

---

## CloudWatch Logs

IncidentHub creates CloudWatch log groups for:

```text
/ecs/incidenthub-dev/api
/ecs/incidenthub-dev/worker
```

CloudWatch Logs can generate cost through:

- log ingestion
- log storage
- retention duration

IncidentHub configures limited retention to reduce long-term cost.

Recommended practice:

- keep retention low in dev
- avoid verbose DEBUG logs in AWS
- destroy log groups when stack is no longer needed

---

## CloudWatch Alarms

V5/V6 create CloudWatch alarms for operational visibility.

Examples:

- ALB 5XX errors
- unhealthy targets
- RDS high CPU
- RDS low free storage
- SQS backlog
- DLQ messages
- oldest queue message age

Alarms are not usually the largest cost item, but they still count as AWS resources.

Destroy the Terraform stack to remove them.

---

## ECR

IncidentHub stores Docker images in ECR repositories:

```text
incidenthub-dev-api
incidenthub-dev-worker
```

ECR cost depends on stored image size and number of images.

Lifecycle policies keep image count limited, but when destroying the stack, ECR repositories must be empty unless `force_delete` is enabled.

If Terraform destroy fails because ECR repositories are not empty, delete the repositories forcefully:

```powershell
aws ecr delete-repository `
  --repository-name incidenthub-dev-api `
  --region eu-west-1 `
  --force

aws ecr delete-repository `
  --repository-name incidenthub-dev-worker `
  --region eu-west-1 `
  --force
```

---

## SNS

IncidentHub uses SNS for real alert delivery.

Cost depends on:

- number of publishes
- delivery protocol
- message volume

For this project, SNS usage is expected to be very low.

If an email subscription was created for demo purposes, it is deleted when the SNS topic is destroyed by Terraform.

---

## SQS

IncidentHub provisions SQS and DLQ foundations.

Current runtime still uses PostgreSQL-backed processing tasks, so SQS traffic should be minimal or zero.

SQS cost is expected to be very low in the current implementation.

---

## NAT Gateway Warning

The current low-cost AWS architecture avoids a NAT Gateway.

A NAT Gateway can become one of the most expensive always-on resources in a small demo project.

If NAT Gateway is introduced in a future version, document it clearly and destroy it when not needed.

---

## Cost Control Strategy

Recommended workflow for demos:

```text
1. terraform apply
2. push images / deploy services
3. run validation scenarios
4. take screenshots
5. set ECS desired count to 0 if pausing briefly
6. terraform destroy when finished
```

For short breaks:

```text
Set ECS desired count to 0.
```

For full cost cleanup:

```text
Run terraform destroy.
```

---

## Full Destroy Command

From:

```powershell
cd infraws\stack
```

Run:

```powershell
terraform destroy
```

Confirm with:

```text
yes
```

If ECR deletion fails because repositories are not empty, force delete the repositories manually and rerun destroy if necessary.

---

## Verify No IncidentHub Resources Remain

Set region:

```powershell
$region = "eu-west-1"
```

### ECR

```powershell
aws ecr describe-repositories `
  --region $region `
  --query "repositories[?contains(repositoryName, 'incidenthub')].repositoryName"
```

Expected:

```json
[]
```

### ECS

```powershell
aws ecs list-clusters `
  --region $region `
  --query "clusterArns[?contains(@, 'incidenthub')]"
```

Expected:

```json
[]
```

### RDS

```powershell
aws rds describe-db-instances `
  --region $region `
  --query "DBInstances[?contains(DBInstanceIdentifier, 'incidenthub')].DBInstanceIdentifier"
```

Expected:

```json
[]
```

### ALB

```powershell
aws elbv2 describe-load-balancers `
  --region $region `
  --query "LoadBalancers[?contains(LoadBalancerName, 'incidenthub')].LoadBalancerName"
```

Expected:

```json
[]
```

### SQS

```powershell
aws sqs list-queues `
  --region $region `
  --queue-name-prefix incidenthub
```

Expected:

```text
No QueueUrls for incidenthub
```

### SNS

```powershell
aws sns list-topics `
  --region $region `
  --query "Topics[?contains(TopicArn, 'incidenthub')].TopicArn"
```

Expected:

```json
[]
```

### CloudWatch Alarms

```powershell
aws cloudwatch describe-alarms `
  --region $region `
  --query "MetricAlarms[?contains(AlarmName, 'incidenthub')].AlarmName"
```

Expected:

```json
[]
```

### IAM Roles

IAM is global:

```powershell
aws iam list-roles `
  --query "Roles[?contains(RoleName, 'incidenthub')].RoleName"
```

Expected:

```json
[]
```

### IAM Policies

```powershell
aws iam list-policies `
  --scope Local `
  --query "Policies[?contains(PolicyName, 'incidenthub')].PolicyName"
```

Expected:

```json
[]
```

---

## Recommended Cost Notes for README

Short version for the main README:

```text
AWS resources are created for demo and validation purposes. To avoid cost, stop ECS services or run terraform destroy when the environment is not needed. RDS, ALB and Fargate are the main cost-sensitive resources.
```

---

## Current Cost Status

After completing V6 validation, the AWS environment was destroyed and checked.

Expected final state:

```text
No IncidentHub ECS clusters
No IncidentHub ECR repositories
No IncidentHub RDS instances
No IncidentHub ALBs
No IncidentHub SQS queues
No IncidentHub SNS topics
No IncidentHub CloudWatch alarms
No IncidentHub IAM roles/policies
```

This confirms the project can be recreated when needed and removed when not in use.
