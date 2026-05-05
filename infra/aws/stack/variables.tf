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