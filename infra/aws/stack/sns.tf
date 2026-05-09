resource "aws_sns_topic" "alerts" {
  name              = "${local.name_prefix}-alerts"
  kms_master_key_id = "alias/aws/sns"

  tags = {
    Name = "${local.name_prefix}-alerts"
  }
}