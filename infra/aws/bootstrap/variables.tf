variable "aws_region" {
  description = "AWS region used for bootstrap resources."
  type        = string
  default     = "eu-west-1"
}

variable "project_name" {
  description = "Project name used for bootstrap resource naming."
  type        = string
  default     = "incidenthub"
}

variable "environment" {
  description = "Environment name."
  type        = string
  default     = "dev"
}

variable "owner" {
  description = "Owner tag for AWS resources."
  type        = string
  default     = "incidenthub"
}