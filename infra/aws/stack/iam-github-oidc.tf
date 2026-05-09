resource "aws_iam_openid_connect_provider" "github" {
  url = "https://token.actions.githubusercontent.com"

  client_id_list = [
    "sts.amazonaws.com"
  ]

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-github-oidc-provider"
  })
}

data "aws_iam_policy_document" "github_actions_assume_role" {
  statement {
    sid     = "AllowGitHubActionsAssumeRole"
    effect  = "Allow"
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.github.arn]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    condition {
      test     = "StringLike"
      variable = "token.actions.githubusercontent.com:sub"
      values = [
        "repo:AlbertoRomaris/incidenthub:ref:refs/heads/main"
      ]
    }
  }
}

resource "aws_iam_role" "github_actions_deployer" {
  name               = "${local.name_prefix}-github-actions-deployer"
  assume_role_policy = data.aws_iam_policy_document.github_actions_assume_role.json

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-github-actions-deployer"
  })
}

data "aws_iam_policy_document" "github_actions_ecr_push" {
  statement {
    sid    = "AllowEcrAuthorization"
    effect = "Allow"

    actions = [
      "ecr:GetAuthorizationToken"
    ]

    resources = ["*"]
  }

  statement {
    sid    = "AllowPushIncidentHubImages"
    effect = "Allow"

    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:BatchGetImage",
      "ecr:CompleteLayerUpload",
      "ecr:DescribeImages",
      "ecr:DescribeRepositories",
      "ecr:GetDownloadUrlForLayer",
      "ecr:InitiateLayerUpload",
      "ecr:PutImage",
      "ecr:UploadLayerPart"
    ]

    resources = [
      aws_ecr_repository.services["api"].arn,
      aws_ecr_repository.services["worker"].arn
    ]
  }
}

resource "aws_iam_policy" "github_actions_ecr_push" {
  name        = "${local.name_prefix}-github-actions-ecr-push-policy"
  description = "Allow GitHub Actions to push IncidentHub API and Worker images to ECR."
  policy      = data.aws_iam_policy_document.github_actions_ecr_push.json

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-github-actions-ecr-push-policy"
  })
}

resource "aws_iam_role_policy_attachment" "github_actions_ecr_push" {
  role       = aws_iam_role.github_actions_deployer.name
  policy_arn = aws_iam_policy.github_actions_ecr_push.arn
}