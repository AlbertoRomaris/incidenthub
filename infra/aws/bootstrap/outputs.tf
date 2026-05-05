output "name_prefix" {
  description = "Resource name prefix for bootstrap resources."
  value       = local.name_prefix
}

output "common_tags" {
  description = "Common tags used by bootstrap resources."
  value       = local.common_tags
}