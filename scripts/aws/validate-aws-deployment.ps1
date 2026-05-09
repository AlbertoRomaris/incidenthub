param(
    [string]$AwsRegion = "eu-west-1",
    [string]$ProjectName = "incidenthub",
    [string]$Environment = "dev",
    [string]$ImageTag = "dev",
    [int]$LogSinceMinutes = 30,
    [string]$OutputDir = ".\diagnostics"
)

$ErrorActionPreference = "Continue"

$NamePrefix = "$ProjectName-$Environment"
$ClusterName = "$NamePrefix-cluster"
$ApiServiceName = "$NamePrefix-api"
$WorkerServiceName = "$NamePrefix-worker"
$ApiAlbName = "$NamePrefix-api-alb"
$ApiTargetGroupName = "$NamePrefix-api-tg"
$ApiRepositoryName = "$NamePrefix-api"
$WorkerRepositoryName = "$NamePrefix-worker"
$ApiLogGroup = "/ecs/$NamePrefix/api"
$WorkerLogGroup = "/ecs/$NamePrefix/worker"

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$Timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$ReportPath = Join-Path $OutputDir "incidenthub-aws-validation-$Timestamp.md"

function Add-Line {
    param([string]$Text = "")
    Add-Content -Path $ReportPath -Value $Text -Encoding UTF8
}

function Add-Section {
    param([string]$Title)
    Add-Line ""
    Add-Line "## $Title"
    Add-Line ""
}

function Run-Command {
    param(
        [string]$Title,
        [scriptblock]$Command
    )

    Add-Section $Title
    Add-Line '```text'

    try {
        $result = & $Command 2>&1 | Out-String
        if ([string]::IsNullOrWhiteSpace($result)) {
            Add-Line "(no output)"
        } else {
            Add-Line $result.TrimEnd()
        }
    } catch {
        Add-Line "ERROR:"
        Add-Line $_.Exception.Message
    }

    Add-Line '```'
}

function Run-RestJson {
    param(
        [string]$Title,
        [string]$Uri
    )

    Add-Section $Title
    Add-Line '```json'

    try {
        $response = Invoke-RestMethod -Uri $Uri -TimeoutSec 20
        $json = $response | ConvertTo-Json -Depth 20
        Add-Line $json
    } catch {
        Add-Line "{"
        Add-Line "  `"error`": `"$($_.Exception.Message.Replace('"', '\"'))`""
        Add-Line "}"
    }

    Add-Line '```'
}

function Run-WebText {
    param(
        [string]$Title,
        [string]$Uri,
        [int]$MaxLines = 120
    )

    Add-Section $Title
    Add-Line '```text'

    try {
        $content = (Invoke-WebRequest -Uri $Uri -TimeoutSec 20).Content
        $lines = $content -split "`n" | Select-Object -First $MaxLines
        Add-Line ($lines -join "`n")
    } catch {
        Add-Line "ERROR:"
        Add-Line $_.Exception.Message
    }

    Add-Line '```'
}

Add-Line "# IncidentHub AWS Validation Report"
Add-Line ""
Add-Line "- Generated at: $(Get-Date -Format o)"
Add-Line "- AWS region: $AwsRegion"
Add-Line "- Project: $ProjectName"
Add-Line "- Environment: $Environment"
Add-Line "- Name prefix: $NamePrefix"
Add-Line "- Cluster: $ClusterName"
Add-Line "- API service: $ApiServiceName"
Add-Line "- Worker service: $WorkerServiceName"
Add-Line "- Image tag: $ImageTag"

Run-Command "AWS identity" {
    aws sts get-caller-identity --region $AwsRegion
}

Run-Command "ECR API image" {
    aws ecr describe-images `
        --repository-name $ApiRepositoryName `
        --image-ids imageTag=$ImageTag `
        --region $AwsRegion
}

Run-Command "ECR Worker image" {
    aws ecr describe-images `
        --repository-name $WorkerRepositoryName `
        --image-ids imageTag=$ImageTag `
        --region $AwsRegion
}

Run-Command "ECS services summary" {
    aws ecs describe-services `
        --cluster $ClusterName `
        --services $ApiServiceName $WorkerServiceName `
        --region $AwsRegion `
        --query "services[*].{name:serviceName,desired:desiredCount,running:runningCount,pending:pendingCount,status:status,taskDefinition:taskDefinition}"
}

Run-Command "ECS latest service events" {
    aws ecs describe-services `
        --cluster $ClusterName `
        --services $ApiServiceName $WorkerServiceName `
        --region $AwsRegion `
        --query "services[*].events[0:8].{message:message,createdAt:createdAt}"
}

Run-Command "ECS running tasks" {
    aws ecs list-tasks `
        --cluster $ClusterName `
        --desired-status RUNNING `
        --region $AwsRegion
}

Run-Command "ECS stopped tasks, recent" {
    aws ecs list-tasks `
        --cluster $ClusterName `
        --desired-status STOPPED `
        --region $AwsRegion `
        --max-items 10
}

Run-Command "ALB details" {
    aws elbv2 describe-load-balancers `
        --names $ApiAlbName `
        --region $AwsRegion `
        --query "LoadBalancers[*].{name:LoadBalancerName,dns:DNSName,state:State.Code,scheme:Scheme,type:Type}"
}

Run-Command "Target group details" {
    aws elbv2 describe-target-groups `
        --names $ApiTargetGroupName `
        --region $AwsRegion `
        --query "TargetGroups[*].{name:TargetGroupName,arn:TargetGroupArn,port:Port,protocol:Protocol,healthPath:HealthCheckPath}"
}

$AlbDns = $null
$TargetGroupArn = $null

try {
    $AlbDns = aws elbv2 describe-load-balancers `
        --names $ApiAlbName `
        --region $AwsRegion `
        --query "LoadBalancers[0].DNSName" `
        --output text
} catch {}

try {
    $TargetGroupArn = aws elbv2 describe-target-groups `
        --names $ApiTargetGroupName `
        --region $AwsRegion `
        --query "TargetGroups[0].TargetGroupArn" `
        --output text
} catch {}

if (-not [string]::IsNullOrWhiteSpace($TargetGroupArn) -and $TargetGroupArn -ne "None") {
    Run-Command "Target health" {
        aws elbv2 describe-target-health `
            --target-group-arn $TargetGroupArn `
            --region $AwsRegion `
            --query "TargetHealthDescriptions[*].{target:Target.Id,port:Target.Port,state:TargetHealth.State,reason:TargetHealth.Reason,description:TargetHealth.Description}"
    }
} else {
    Add-Section "Target health"
    Add-Line '```text'
    Add-Line "Could not resolve target group ARN."
    Add-Line '```'
}

if (-not [string]::IsNullOrWhiteSpace($AlbDns) -and $AlbDns -ne "None") {
    $BaseUrl = "http://$AlbDns"

    Run-RestJson "API health via ALB" "$BaseUrl/actuator/health"
    Run-RestJson "Operational metrics via ALB" "$BaseUrl/metrics/operational"
    Run-RestJson "SLO summary via ALB" "$BaseUrl/slo/summary"
    Run-WebText "Prometheus metrics via ALB" "$BaseUrl/metrics/prometheus" 160
} else {
    Add-Section "API HTTP checks"
    Add-Line '```text'
    Add-Line "Could not resolve ALB DNS."
    Add-Line '```'
}

Run-Command "CloudWatch API logs" {
    aws logs tail $ApiLogGroup `
        --region $AwsRegion `
        --since "${LogSinceMinutes}m"
}

Run-Command "CloudWatch Worker logs" {
    aws logs tail $WorkerLogGroup `
        --region $AwsRegion `
        --since "${LogSinceMinutes}m"
}

Run-Command "CloudWatch alarm states" {
    aws cloudwatch describe-alarms `
        --region $AwsRegion `
        --alarm-name-prefix $NamePrefix `
        --query "MetricAlarms[*].{name:AlarmName,state:StateValue,reason:StateReason}"
}

Run-Command "SQS queues" {
    aws sqs list-queues `
        --queue-name-prefix $NamePrefix `
        --region $AwsRegion
}

Run-Command "SNS topics filtered" {
    aws sns list-topics `
        --region $AwsRegion `
        --query "Topics[?contains(TopicArn, '$NamePrefix')]"
}

Add-Line ""
Add-Line "---"
Add-Line ""
Add-Line "Report file:"
Add-Line ""
Add-Line '```text'
Add-Line $ReportPath
Add-Line '```'

Write-Host ""
Write-Host "Validation report generated:"
Write-Host $ReportPath
Write-Host ""
Write-Host "You can open it or paste its content here."
