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

output "vpc_id" {
  description = "IncidentHub VPC id."
  value       = aws_vpc.main.id
}

output "vpc_cidr_block" {
  description = "IncidentHub VPC CIDR block."
  value       = aws_vpc.main.cidr_block
}

output "public_subnet_ids" {
  description = "Public subnet ids."
  value       = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  description = "Private subnet ids."
  value       = aws_subnet.private[*].id
}

output "availability_zones" {
  description = "Availability zones used by the stack."
  value       = local.selected_availability_zones
}