locals {
  ecr_repositories = {
    api = {
      name = "${local.name_prefix}-api"
    }

    worker = {
      name = "${local.name_prefix}-worker"
    }
  }
}

resource "aws_ecr_repository" "services" {
  for_each = local.ecr_repositories

  name                 = each.value.name
  image_tag_mutability = var.ecr_image_tag_mutability

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }
}

resource "aws_ecr_lifecycle_policy" "services" {
  for_each = aws_ecr_repository.services

  repository = each.value.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep only the most recent tagged images"
        selection = {
          tagStatus     = "tagged"
          tagPrefixList = ["v", "main", "dev", "latest"]
          countType     = "imageCountMoreThan"
          countNumber   = var.ecr_lifecycle_keep_last_images
        }
        action = {
          type = "expire"
        }
      },
      {
        rulePriority = 2
        description  = "Expire untagged images older than 7 days"
        selection = {
          tagStatus   = "untagged"
          countType   = "sinceImagePushed"
          countUnit   = "days"
          countNumber = 7
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}