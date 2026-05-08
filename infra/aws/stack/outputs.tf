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

output "security_group_ids" {
  description = "Security group ids used by the IncidentHub stack."
  value = {
    alb      = aws_security_group.alb.id
    api      = aws_security_group.api.id
    worker   = aws_security_group.worker.id
    database = aws_security_group.database.id
  }
}

output "database_endpoint" {
  description = "RDS PostgreSQL endpoint."
  value       = aws_db_instance.postgres.address
}

output "database_port" {
  description = "RDS PostgreSQL port."
  value       = aws_db_instance.postgres.port
}

output "database_name" {
  description = "IncidentHub database name."
  value       = aws_db_instance.postgres.db_name
}

output "database_username" {
  description = "IncidentHub database username."
  value       = aws_db_instance.postgres.username
  sensitive   = true
}

output "database_master_user_secret_arn" {
  description = "ARN of the RDS-managed master user secret."
  value       = aws_db_instance.postgres.master_user_secret[0].secret_arn
  sensitive   = true
}