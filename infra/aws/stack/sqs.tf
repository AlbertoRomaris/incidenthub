resource "aws_sqs_queue" "signal_processing_dlq" {
  name = "${local.name_prefix}-signal-processing-dlq"

  message_retention_seconds = var.signal_queue_message_retention_seconds
  sqs_managed_sse_enabled   = true

  tags = {
    Name = "${local.name_prefix}-signal-processing-dlq"
  }
}

resource "aws_sqs_queue" "signal_processing" {
  name = "${local.name_prefix}-signal-processing"

  visibility_timeout_seconds = var.signal_queue_visibility_timeout_seconds
  message_retention_seconds  = var.signal_queue_message_retention_seconds
  sqs_managed_sse_enabled    = true

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.signal_processing_dlq.arn
    maxReceiveCount     = var.signal_queue_dlq_max_receive_count
  })

  tags = {
    Name = "${local.name_prefix}-signal-processing"
  }
}