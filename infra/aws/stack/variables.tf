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