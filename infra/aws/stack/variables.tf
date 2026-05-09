variable "aws_region" {
  description = "AWS region where IncidentHub stack resources will be deployed."
  type        = string
  default     = "eu-west-1"
}

variable "project_name" {
  description = "Project name used for AWS resource naming."
  type        = string
  default     = "incidenthub"
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
  default     = "dev"
}

variable "owner" {
  description = "Owner tag for AWS resources."
  type        = string
  default     = "incidenthub"
}

variable "ecr_image_tag_mutability" {
  description = "Image tag mutability setting for ECR repositories."
  type        = string
  default     = "MUTABLE"

  validation {
    condition     = contains(["MUTABLE", "IMMUTABLE"], var.ecr_image_tag_mutability)
    error_message = "ECR image tag mutability must be either MUTABLE or IMMUTABLE."
  }
}

variable "ecr_lifecycle_keep_last_images" {
  description = "Number of recent tagged images to keep in each ECR repository."
  type        = number
  default     = 20

  validation {
    condition     = var.ecr_lifecycle_keep_last_images > 0
    error_message = "The number of ECR images to keep must be greater than zero."
  }
}

variable "vpc_cidr" {
  description = "CIDR block for the IncidentHub VPC."
  type        = string
  default     = "10.20.0.0/16"
}

variable "availability_zone_count" {
  description = "Number of availability zones to use."
  type        = number
  default     = 2

  validation {
    condition     = var.availability_zone_count >= 2 && var.availability_zone_count <= 3
    error_message = "availability_zone_count must be between 2 and 3."
  }
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets."
  type        = list(string)
  default     = ["10.20.0.0/24", "10.20.1.0/24", "10.20.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets."
  type        = list(string)
  default     = ["10.20.10.0/24", "10.20.11.0/24", "10.20.12.0/24"]
}

variable "enable_nat_gateway" {
  description = "Whether to create a NAT Gateway for private subnet outbound internet access. Disabled by default to avoid cost."
  type        = bool
  default     = false
}

variable "allowed_http_cidr_blocks" {
  description = "CIDR blocks allowed to access the public ALB over HTTP."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "api_container_port" {
  description = "Container port exposed by the IncidentHub API."
  type        = number
  default     = 8080
}

variable "database_port" {
  description = "PostgreSQL database port."
  type        = number
  default     = 5432
}

variable "database_name" {
  description = "PostgreSQL database name used by IncidentHub."
  type        = string
  default     = "incidenthub"
}

variable "database_username" {
  description = "PostgreSQL master username used by IncidentHub."
  type        = string
  default     = "incidenthub"
}

variable "database_instance_class" {
  description = "RDS PostgreSQL instance class."
  type        = string
  default     = "db.t4g.micro"
}

variable "database_allocated_storage" {
  description = "Allocated storage in GB for the RDS PostgreSQL instance."
  type        = number
  default     = 20
}

variable "database_max_allocated_storage" {
  description = "Maximum allocated storage in GB for RDS autoscaling."
  type        = number
  default     = 50
}

variable "database_engine_version" {
  description = "PostgreSQL engine version."
  type        = string
  default     = "16"
}

variable "database_backup_retention_days" {
  description = "Number of days to retain automated database backups."
  type        = number
  default     = 7
}

variable "database_deletion_protection" {
  description = "Whether deletion protection is enabled for the database."
  type        = bool
  default     = false
}

variable "database_skip_final_snapshot" {
  description = "Whether to skip the final snapshot when destroying the dev database."
  type        = bool
  default     = true
}

variable "ecs_container_insights_enabled" {
  description = "Whether ECS Container Insights should be enabled."
  type        = bool
  default     = false
}

variable "cloudwatch_log_retention_days" {
  description = "Number of days to retain CloudWatch logs."
  type        = number
  default     = 14
}

variable "alb_enable_deletion_protection" {
  description = "Whether deletion protection is enabled for the API Application Load Balancer."
  type        = bool
  default     = false
}

variable "api_health_check_path" {
  description = "Health check path used by the API target group."
  type        = string
  default     = "/actuator/health"
}

variable "api_image_tag" {
  description = "Docker image tag used by the IncidentHub API ECS task."
  type        = string
  default     = "dev"
}

variable "api_desired_count" {
  description = "Desired number of IncidentHub API tasks."
  type        = number
  default     = 1
}

variable "api_task_cpu" {
  description = "CPU units for the IncidentHub API Fargate task."
  type        = number
  default     = 512
}

variable "api_task_memory" {
  description = "Memory in MiB for the IncidentHub API Fargate task."
  type        = number
  default     = 1024
}

variable "api_health_check_grace_period_seconds" {
  description = "Health check grace period for the IncidentHub API ECS service."
  type        = number
  default     = 60
}

variable "worker_image_tag" {
  description = "Docker image tag used by the IncidentHub Worker ECS task."
  type        = string
  default     = "dev"
}

variable "worker_desired_count" {
  description = "Desired number of IncidentHub Worker tasks."
  type        = number
  default     = 1
}

variable "worker_task_cpu" {
  description = "CPU units for the IncidentHub Worker Fargate task."
  type        = number
  default     = 512
}

variable "worker_task_memory" {
  description = "Memory in MiB for the IncidentHub Worker Fargate task."
  type        = number
  default     = 1024
}

variable "worker_batch_size" {
  description = "Number of signal processing tasks claimed per Worker polling cycle."
  type        = number
  default     = 10
}

variable "worker_poll_delay_ms" {
  description = "Delay in milliseconds between Worker signal processing polling cycles."
  type        = number
  default     = 5000
}

variable "alerts_batch_size" {
  description = "Number of pending alerts claimed per Worker polling cycle."
  type        = number
  default     = 10
}

variable "alerts_poll_delay_ms" {
  description = "Delay in milliseconds between Worker alert delivery polling cycles."
  type        = number
  default     = 5000
}

variable "signal_queue_visibility_timeout_seconds" {
  description = "Visibility timeout in seconds for the SQS signal processing queue."
  type        = number
  default     = 60
}

variable "signal_queue_message_retention_seconds" {
  description = "Message retention in seconds for the SQS signal processing queue."
  type        = number
  default     = 345600
}

variable "signal_queue_dlq_max_receive_count" {
  description = "Number of receives before a signal queue message is moved to the DLQ."
  type        = number
  default     = 5
}