resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${local.name_prefix}-runtime-dashboard"

  dashboard_body = jsonencode({
    widgets = [
      {
        type   = "text"
        x      = 0
        y      = 0
        width  = 24
        height = 2

        properties = {
          markdown = "# IncidentHub ${upper(var.environment)} Runtime Dashboard\\nAWS runtime health for ALB, ECS, RDS and SQS."
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 2
        width  = 12
        height = 6

        properties = {
          title   = "ALB Requests and 2XX Responses"
          region  = var.aws_region
          view    = "timeSeries"
          stacked = false
          period  = 60
          stat    = "Sum"

          metrics = [
            [
              "AWS/ApplicationELB",
              "RequestCount",
              "LoadBalancer",
              aws_lb.api.arn_suffix,
              {
                label = "Requests"
              }
            ],
            [
              ".",
              "HTTPCode_Target_2XX_Count",
              ".",
              ".",
              {
                label = "Target 2XX"
              }
            ],
            [
              ".",
              "HTTPCode_ELB_5XX_Count",
              ".",
              ".",
              {
                label = "ALB 5XX"
              }
            ]
          ]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 2
        width  = 12
        height = 6

        properties = {
          title   = "ALB Target Health"
          region  = var.aws_region
          view    = "timeSeries"
          stacked = false
          period  = 60
          stat    = "Average"

          metrics = [
            [
              "AWS/ApplicationELB",
              "HealthyHostCount",
              "TargetGroup",
              aws_lb_target_group.api.arn_suffix,
              "LoadBalancer",
              aws_lb.api.arn_suffix,
              {
                label = "Healthy targets"
              }
            ],
            [
              ".",
              "UnHealthyHostCount",
              ".",
              ".",
              ".",
              ".",
              {
                label = "Unhealthy targets"
              }
            ]
          ]
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 8
        width  = 12
        height = 6

        properties = {
          title   = "ALB Target Response Time"
          region  = var.aws_region
          view    = "timeSeries"
          stacked = false
          period  = 60
          stat    = "Average"

          metrics = [
            [
              "AWS/ApplicationELB",
              "TargetResponseTime",
              "LoadBalancer",
              aws_lb.api.arn_suffix,
              {
                label = "Target response time"
              }
            ]
          ]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 8
        width  = 12
        height = 6

        properties = {
          title   = "ECS API CPU and Memory"
          region  = var.aws_region
          view    = "timeSeries"
          stacked = false
          period  = 60
          stat    = "Average"

          metrics = [
            [
              "AWS/ECS",
              "CPUUtilization",
              "ClusterName",
              aws_ecs_cluster.main.name,
              "ServiceName",
              aws_ecs_service.api.name,
              {
                label = "API CPU"
              }
            ],
            [
              ".",
              "MemoryUtilization",
              ".",
              ".",
              ".",
              ".",
              {
                label = "API Memory"
              }
            ]
          ]
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 14
        width  = 12
        height = 6

        properties = {
          title   = "ECS Worker CPU and Memory"
          region  = var.aws_region
          view    = "timeSeries"
          stacked = false
          period  = 60
          stat    = "Average"

          metrics = [
            [
              "AWS/ECS",
              "CPUUtilization",
              "ClusterName",
              aws_ecs_cluster.main.name,
              "ServiceName",
              aws_ecs_service.worker.name,
              {
                label = "Worker CPU"
              }
            ],
            [
              ".",
              "MemoryUtilization",
              ".",
              ".",
              ".",
              ".",
              {
                label = "Worker Memory"
              }
            ]
          ]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 14
        width  = 12
        height = 6

        properties = {
          title   = "RDS CPU and Connections"
          region  = var.aws_region
          view    = "timeSeries"
          stacked = false
          period  = 60
          stat    = "Average"

          metrics = [
            [
              "AWS/RDS",
              "CPUUtilization",
              "DBInstanceIdentifier",
              aws_db_instance.postgres.identifier,
              {
                label = "RDS CPU"
              }
            ],
            [
              ".",
              "DatabaseConnections",
              ".",
              ".",
              {
                label = "DB connections"
              }
            ]
          ]
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 20
        width  = 12
        height = 6

        properties = {
          title   = "RDS Free Storage"
          region  = var.aws_region
          view    = "timeSeries"
          stacked = false
          period  = 300
          stat    = "Average"

          metrics = [
            [
              "AWS/RDS",
              "FreeStorageSpace",
              "DBInstanceIdentifier",
              aws_db_instance.postgres.identifier,
              {
                label = "Free storage"
              }
            ]
          ]
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 20
        width  = 12
        height = 6

        properties = {
          title   = "SQS Signal Queue and DLQ"
          region  = var.aws_region
          view    = "timeSeries"
          stacked = false
          period  = 60
          stat    = "Average"

          metrics = [
            [
              "AWS/SQS",
              "ApproximateNumberOfMessagesVisible",
              "QueueName",
              aws_sqs_queue.signal_processing.name,
              {
                label = "Signal queue visible messages"
              }
            ],
            [
              ".",
              "ApproximateAgeOfOldestMessage",
              ".",
              ".",
              {
                label = "Oldest signal message age"
              }
            ],
            [
              ".",
              "ApproximateNumberOfMessagesVisible",
              ".",
              aws_sqs_queue.signal_processing_dlq.name,
              {
                label = "DLQ visible messages"
              }
            ]
          ]
        }
      }
    ]
  })
}