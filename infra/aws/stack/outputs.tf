output "aws_account_id" {
  description = "AWS account id detected by the provider."
  value       = data.aws_caller_identity.current.account_id
}

output "aws_region" {
  description = "AWS region detected by the provider."
  value       = data.aws_region.current.name
}

output "name_prefix" {
  description = "Resource name prefix for this stack."
  value       = local.name_prefix
}

output "common_tags" {
  description = "Common tags applied to stack resources."
  value       = local.common_tags
}

output "ecr_repository_urls" {
  description = "ECR repository URLs by service."
  value = {
    for service_name, repository in aws_ecr_repository.services :
    service_name => repository.repository_url
  }
}

output "ecr_repository_names" {
  description = "ECR repository names by service."
  value = {
    for service_name, repository in aws_ecr_repository.services :
    service_name => repository.name
  }
}