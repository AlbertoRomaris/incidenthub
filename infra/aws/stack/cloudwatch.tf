resource "aws_cloudwatch_log_group" "api" {
  name              = "/ecs/${local.name_prefix}/api"
  retention_in_days = var.cloudwatch_log_retention_days

  tags = {
    Name = "${local.name_prefix}-api-logs"
  }
}

resource "aws_cloudwatch_log_group" "worker" {
  name              = "/ecs/${local.name_prefix}/worker"
  retention_in_days = var.cloudwatch_log_retention_days

  tags = {
    Name = "${local.name_prefix}-worker-logs"
  }
}

resource "aws_cloudwatch_metric_alarm" "api_alb_5xx" {
  alarm_name          = "${local.name_prefix}-api-alb-5xx"
  alarm_description   = "API ALB is returning too many 5XX responses."
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 1
  threshold           = var.alb_5xx_alarm_threshold
  period              = 60
  statistic           = "Sum"
  namespace           = "AWS/ApplicationELB"
  metric_name         = "HTTPCode_ELB_5XX_Count"
  treat_missing_data  = "notBreaching"
  alarm_actions       = var.alarm_actions

  dimensions = {
    LoadBalancer = aws_lb.api.arn_suffix
  }

  tags = {
    Name = "${local.name_prefix}-api-alb-5xx"
  }
}

resource "aws_cloudwatch_metric_alarm" "api_unhealthy_hosts" {
  alarm_name          = "${local.name_prefix}-api-unhealthy-hosts"
  alarm_description   = "API target group has unhealthy ECS targets."
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  threshold           = 1
  period              = 60
  statistic           = "Average"
  namespace           = "AWS/ApplicationELB"
  metric_name         = "UnHealthyHostCount"
  treat_missing_data  = "notBreaching"
  alarm_actions       = var.alarm_actions

  dimensions = {
    LoadBalancer = aws_lb.api.arn_suffix
    TargetGroup  = aws_lb_target_group.api.arn_suffix
  }

  tags = {
    Name = "${local.name_prefix}-api-unhealthy-hosts"
  }
}

resource "aws_cloudwatch_metric_alarm" "rds_high_cpu" {
  alarm_name          = "${local.name_prefix}-rds-high-cpu"
  alarm_description   = "RDS PostgreSQL CPU utilization is high."
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 3
  threshold           = var.rds_cpu_alarm_threshold
  period              = 300
  statistic           = "Average"
  namespace           = "AWS/RDS"
  metric_name         = "CPUUtilization"
  treat_missing_data  = "notBreaching"
  alarm_actions       = var.alarm_actions

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.postgres.identifier
  }

  tags = {
    Name = "${local.name_prefix}-rds-high-cpu"
  }
}

resource "aws_cloudwatch_metric_alarm" "rds_low_free_storage" {
  alarm_name          = "${local.name_prefix}-rds-low-free-storage"
  alarm_description   = "RDS PostgreSQL free storage is low."
  comparison_operator = "LessThanOrEqualToThreshold"
  evaluation_periods  = 2
  threshold           = var.rds_free_storage_alarm_threshold_bytes
  period              = 300
  statistic           = "Average"
  namespace           = "AWS/RDS"
  metric_name         = "FreeStorageSpace"
  treat_missing_data  = "notBreaching"
  alarm_actions       = var.alarm_actions

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.postgres.identifier
  }

  tags = {
    Name = "${local.name_prefix}-rds-low-free-storage"
  }
}

resource "aws_cloudwatch_metric_alarm" "signal_queue_visible_messages" {
  alarm_name          = "${local.name_prefix}-signal-queue-backlog"
  alarm_description   = "Signal processing queue has too many visible messages."
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  threshold           = var.sqs_visible_messages_alarm_threshold
  period              = 60
  statistic           = "Average"
  namespace           = "AWS/SQS"
  metric_name         = "ApproximateNumberOfMessagesVisible"
  treat_missing_data  = "notBreaching"
  alarm_actions       = var.alarm_actions

  dimensions = {
    QueueName = aws_sqs_queue.signal_processing.name
  }

  tags = {
    Name = "${local.name_prefix}-signal-queue-backlog"
  }
}

resource "aws_cloudwatch_metric_alarm" "signal_queue_oldest_message_age" {
  alarm_name          = "${local.name_prefix}-signal-queue-oldest-message-age"
  alarm_description   = "Oldest signal processing message is too old."
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  threshold           = var.sqs_oldest_message_age_alarm_threshold_seconds
  period              = 60
  statistic           = "Maximum"
  namespace           = "AWS/SQS"
  metric_name         = "ApproximateAgeOfOldestMessage"
  treat_missing_data  = "notBreaching"
  alarm_actions       = var.alarm_actions

  dimensions = {
    QueueName = aws_sqs_queue.signal_processing.name
  }

  tags = {
    Name = "${local.name_prefix}-signal-queue-oldest-message-age"
  }
}

resource "aws_cloudwatch_metric_alarm" "signal_processing_dlq_messages" {
  alarm_name          = "${local.name_prefix}-signal-processing-dlq-messages"
  alarm_description   = "Signal processing DLQ contains messages."
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 1
  threshold           = 1
  period              = 60
  statistic           = "Average"
  namespace           = "AWS/SQS"
  metric_name         = "ApproximateNumberOfMessagesVisible"
  treat_missing_data  = "notBreaching"
  alarm_actions       = var.alarm_actions

  dimensions = {
    QueueName = aws_sqs_queue.signal_processing_dlq.name
  }

  tags = {
    Name = "${local.name_prefix}-signal-processing-dlq-messages"
  }
}