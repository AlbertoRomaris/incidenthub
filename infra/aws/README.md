# IncidentHub AWS Infrastructure

This directory contains Terraform configuration for deploying IncidentHub to AWS.

## Structure

```text
infra/aws/
├── bootstrap/
└── stack/
```

## Bootstrap

The `bootstrap` directory is reserved for foundational resources required before the main stack can use remote Terraform state or CI/CD.

Typical bootstrap resources:

- S3 bucket for Terraform state
- DynamoDB table for state locking
- GitHub Actions OIDC provider
- CI/CD IAM roles

## Stack

The `stack` directory contains the main IncidentHub AWS infrastructure.

Planned resources:

- ECR repositories for API and Worker images
- VPC networking
- ECS Fargate API service
- ECS Fargate Worker service
- RDS PostgreSQL
- SQS signal queue
- SNS alert topic
- CloudWatch log groups and alarms
- GitHub Actions CI/CD with OIDC

## Current Status

This is an initial skeleton.

It defines:

- Terraform provider configuration
- common variables
- common tags
- naming convention
- planned stack files

It does not create billable AWS resources yet.

## Local Validation

From either `bootstrap` or `stack`:

```bash
terraform init
terraform fmt
terraform validate
terraform plan
```

## Naming Convention

Resources should use:

```text
${project_name}-${environment}-<resource-name>
```

Example:

```text
incidenthub-dev-api
incidenthub-dev-worker
incidenthub-dev-postgres
```

## Tagging

All resources should include:

```text
Project
Environment
Owner
ManagedBy
```
