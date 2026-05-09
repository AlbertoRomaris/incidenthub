data "aws_iam_policy_document" "ecs_tasks_assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }

    actions = [
      "sts:AssumeRole"
    ]
  }
}

resource "aws_iam_role" "ecs_task_execution" {
  name               = "${local.name_prefix}-ecs-task-execution-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume_role.json

  tags = {
    Name = "${local.name_prefix}-ecs-task-execution-role"
  }
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_managed" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

data "aws_iam_policy_document" "ecs_task_execution_secrets" {
  statement {
    sid    = "AllowReadDatabaseSecret"
    effect = "Allow"

    actions = [
      "secretsmanager:GetSecretValue"
    ]

    resources = [
      aws_db_instance.postgres.master_user_secret[0].secret_arn
    ]
  }
}

resource "aws_iam_policy" "ecs_task_execution_secrets" {
  name        = "${local.name_prefix}-ecs-task-execution-secrets-policy"
  description = "Allow ECS task execution role to read runtime secrets."
  policy      = data.aws_iam_policy_document.ecs_task_execution_secrets.json

  tags = {
    Name = "${local.name_prefix}-ecs-task-execution-secrets-policy"
  }
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_secrets" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = aws_iam_policy.ecs_task_execution_secrets.arn
}

resource "aws_iam_role" "ecs_task" {
  name               = "${local.name_prefix}-ecs-task-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume_role.json

  tags = {
    Name = "${local.name_prefix}-ecs-task-role"
  }
}

data "aws_iam_policy_document" "ecs_task_messaging" {
  statement {
    sid    = "AllowSignalQueueAccess"
    effect = "Allow"

    actions = [
      "sqs:GetQueueAttributes",
      "sqs:GetQueueUrl",
      "sqs:SendMessage",
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:ChangeMessageVisibility"
    ]

    resources = [
      aws_sqs_queue.signal_processing.arn,
      aws_sqs_queue.signal_processing_dlq.arn
    ]
  }

  statement {
    sid    = "AllowAlertPublishing"
    effect = "Allow"

    actions = [
      "sns:Publish"
    ]

    resources = [
      aws_sns_topic.alerts.arn
    ]
  }
}

resource "aws_iam_policy" "ecs_task_messaging" {
  name        = "${local.name_prefix}-ecs-task-messaging-policy"
  description = "Allow IncidentHub ECS tasks to use SQS signal queue and SNS alert topic."
  policy      = data.aws_iam_policy_document.ecs_task_messaging.json

  tags = {
    Name = "${local.name_prefix}-ecs-task-messaging-policy"
  }
}

resource "aws_iam_role_policy_attachment" "ecs_task_messaging" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.ecs_task_messaging.arn
}