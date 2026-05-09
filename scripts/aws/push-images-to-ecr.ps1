param(
    [string]$AwsRegion = "eu-west-1",
    [string]$ProjectName = "incidenthub",
    [string]$Environment = "dev",
    [string]$ImageTag = "dev"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Invoke-NativeCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Description,

        [Parameter(Mandatory = $true)]
        [scriptblock]$Command
    )

    Write-Host ""
    Write-Host "==> $Description"

    & $Command

    if ($LASTEXITCODE -ne 0) {
        throw "Command failed: $Description. Exit code: $LASTEXITCODE"
    }
}

Write-Host "Resolving AWS account id..."

$AwsAccountId = aws sts get-caller-identity --query Account --output text --region $AwsRegion

if ([string]::IsNullOrWhiteSpace($AwsAccountId)) {
    throw "Could not resolve AWS account id."
}

$Registry = "$AwsAccountId.dkr.ecr.$AwsRegion.amazonaws.com"

$ApiRepositoryName = "$ProjectName-$Environment-api"
$WorkerRepositoryName = "$ProjectName-$Environment-worker"

$ApiImage = "$Registry/$ApiRepositoryName`:$ImageTag"
$WorkerImage = "$Registry/$WorkerRepositoryName`:$ImageTag"

Write-Host "AWS Account: $AwsAccountId"
Write-Host "AWS Region : $AwsRegion"
Write-Host "Registry   : $Registry"
Write-Host "API image  : $ApiImage"
Write-Host "Worker img : $WorkerImage"
Write-Host "Image tag  : $ImageTag"

Invoke-NativeCommand "Authenticating Docker with Amazon ECR" {
    cmd /c "aws ecr get-login-password --region $AwsRegion | docker login --username AWS --password-stdin $Registry"
}

Invoke-NativeCommand "Checking API ECR repository exists" {
    aws ecr describe-repositories `
        --repository-names $ApiRepositoryName `
        --region $AwsRegion `
        --output text | Out-Null
}

Invoke-NativeCommand "Checking Worker ECR repository exists" {
    aws ecr describe-repositories `
        --repository-names $WorkerRepositoryName `
        --region $AwsRegion `
        --output text | Out-Null
}

Invoke-NativeCommand "Building IncidentHub API image" {
    docker build `
        -f incidenthub-api/Dockerfile `
        -t incidenthub-api:local `
        .
}

Invoke-NativeCommand "Building IncidentHub Worker image" {
    docker build `
        -f incidenthub-worker/Dockerfile `
        -t incidenthub-worker:local `
        .
}

Invoke-NativeCommand "Tagging API image" {
    docker tag incidenthub-api:local $ApiImage
}

Invoke-NativeCommand "Tagging Worker image" {
    docker tag incidenthub-worker:local $WorkerImage
}

Invoke-NativeCommand "Pushing API image to ECR" {
    docker push $ApiImage
}

Invoke-NativeCommand "Pushing Worker image to ECR" {
    docker push $WorkerImage
}

Write-Host ""
Write-Host "Published images:"
Write-Host "  $ApiImage"
Write-Host "  $WorkerImage"
Write-Host ""
Write-Host "Done."