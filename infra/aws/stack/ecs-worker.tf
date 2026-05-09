locals {
  worker_container_name = "incidenthub-worker"

  worker_subnet_ids = var.enable_nat_gateway ? aws_subnet.private[*].id : aws_subnet.public[*].id

  worker_assign_public_ip = var.enable_nat_gateway ? false : true

  worker_database_url = "jdbc:postgresql://${aws_db_instance.postgres.address}:${aws_db_instance.postgres.port}/${aws_db_instance.postgres.db_name}"
}

resource "aws_ecs_task_definition" "worker" {
  family                   = "${local.name_prefix}-worker"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"

  cpu    = var.worker_task_cpu
  memory = var.worker_task_memory

  execution_role_arn = aws_iam_role.ecs_task_execution.arn
  task_role_arn      = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name      = local.worker_container_name
      image     = "${aws_ecr_repository.services["worker"].repository_url}:${var.worker_image_tag}"
      essential = true

      environment = [
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = "cloud"
        },
        {
          name  = "SPRING_DATASOURCE_URL"
          value = local.worker_database_url
        },
        {
          name  = "SPRING_DATASOURCE_USERNAME"
          value = var.database_username
        },
        {
          name  = "INCIDENTHUB_WORKER_ID"
          value = "${local.name_prefix}-worker"
        },
        {
          name  = "INCIDENTHUB_WORKER_BATCH_SIZE"
          value = tostring(var.worker_batch_size)
        },
        {
          name  = "INCIDENTHUB_WORKER_POLL_DELAY_MS"
          value = tostring(var.worker_poll_delay_ms)
        },
        {
          name  = "INCIDENTHUB_ALERTS_BATCH_SIZE"
          value = tostring(var.alerts_batch_size)
        },
        {
          name  = "INCIDENTHUB_ALERTS_POLL_DELAY_MS"
          value = tostring(var.alerts_poll_delay_ms)
        },
        {
          name  = "LOGGING_LEVEL_ROOT"
          value = "INFO"
        },
        {
          name  = "LOGGING_LEVEL_INCIDENTHUB"
          value = "INFO"
        }
      ]

      secrets = [
        {
          name      = "SPRING_DATASOURCE_PASSWORD"
          valueFrom = "${aws_db_instance.postgres.master_user_secret[0].secret_arn}:password::"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"

        options = {
          awslogs-group         = aws_cloudwatch_log_group.worker.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "worker"
        }
      }
    }
  ])

  tags = {
    Name = "${local.name_prefix}-worker-task"
  }
}

resource "aws_ecs_service" "worker" {
  name            = "${local.name_prefix}-worker"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.worker.arn
  desired_count   = var.worker_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = local.worker_subnet_ids
    security_groups  = [aws_security_group.worker.id]
    assign_public_ip = local.worker_assign_public_ip
  }

  depends_on = [
    aws_iam_role_policy_attachment.ecs_task_execution_managed,
    aws_iam_role_policy_attachment.ecs_task_execution_secrets
  ]

  tags = {
    Name = "${local.name_prefix}-worker-service"
  }
}