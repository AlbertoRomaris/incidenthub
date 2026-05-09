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

output "ecs_cluster_name" {
  description = "ECS cluster name."
  value       = aws_ecs_cluster.main.name
}

output "ecs_cluster_arn" {
  description = "ECS cluster ARN."
  value       = aws_ecs_cluster.main.arn
}

output "cloudwatch_log_group_names" {
  description = "CloudWatch log group names used by ECS services."
  value = {
    api    = aws_cloudwatch_log_group.api.name
    worker = aws_cloudwatch_log_group.worker.name
  }
}

output "ecs_task_execution_role_arn" {
  description = "IAM role ARN used by ECS to pull images, write logs and read secrets."
  value       = aws_iam_role.ecs_task_execution.arn
}

output "ecs_task_role_arn" {
  description = "IAM role ARN assumed by IncidentHub containers at runtime."
  value       = aws_iam_role.ecs_task.arn
}

output "api_load_balancer_dns_name" {
  description = "Public DNS name of the API Application Load Balancer."
  value       = aws_lb.api.dns_name
}

output "api_load_balancer_arn" {
  description = "ARN of the API Application Load Balancer."
  value       = aws_lb.api.arn
}

output "api_target_group_arn" {
  description = "ARN of the API target group."
  value       = aws_lb_target_group.api.arn
}

output "api_http_listener_arn" {
  description = "ARN of the API HTTP listener."
  value       = aws_lb_listener.api_http.arn
}

output "api_task_definition_arn" {
  description = "IncidentHub API ECS task definition ARN."
  value       = aws_ecs_task_definition.api.arn
}

output "api_ecs_service_name" {
  description = "IncidentHub API ECS service name."
  value       = aws_ecs_service.api.name
}

output "worker_task_definition_arn" {
  description = "IncidentHub Worker ECS task definition ARN."
  value       = aws_ecs_task_definition.worker.arn
}

output "worker_ecs_service_name" {
  description = "IncidentHub Worker ECS service name."
  value       = aws_ecs_service.worker.name
}