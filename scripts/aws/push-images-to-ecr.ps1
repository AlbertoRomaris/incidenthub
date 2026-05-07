param(
    [string]$AwsRegion = "eu-west-1",
    [string]$ProjectName = "incidenthub",
    [string]$Environment = "dev",
    [string]$ImageTag = "dev",
    [string]$AwsProfile = ""
)

$ErrorActionPreference = "Stop"

$awsProfileArgs = @()

if (-not [string]::IsNullOrWhiteSpace($AwsProfile)) {
    $awsProfileArgs = @("--profile", $AwsProfile)
}

Write-Host "Resolving AWS account id..."
$AwsAccountId = aws sts get-caller-identity @awsProfileArgs --query Account --output text

if ([string]::IsNullOrWhiteSpace($AwsAccountId)) {
    throw "Could not resolve AWS account id. Check your AWS credentials."
}

$Registry = "$AwsAccountId.dkr.ecr.$AwsRegion.amazonaws.com"
$ApiRepository = "$Registry/$ProjectName-$Environment-api"
$WorkerRepository = "$Registry/$ProjectName-$Environment-worker"

Write-Host "AWS Account: $AwsAccountId"
Write-Host "AWS Region : $AwsRegion"
Write-Host "Registry   : $Registry"
Write-Host "API repo   : $ApiRepository"
Write-Host "Worker repo: $WorkerRepository"
Write-Host "Image tag  : $ImageTag"

Write-Host "Authenticating Docker with Amazon ECR..."
aws ecr get-login-password --region $AwsRegion @awsProfileArgs |
    docker login --username AWS --password-stdin $Registry

Write-Host "Checking ECR repositories exist..."
aws ecr describe-repositories `
    --region $AwsRegion `
    --repository-names "$ProjectName-$Environment-api" "$ProjectName-$Environment-worker" `
    @awsProfileArgs | Out-Null

Write-Host "Building IncidentHub API image..."
docker build -f incidenthub-api/Dockerfile -t incidenthub-api:local .

Write-Host "Building IncidentHub Worker image..."
docker build -f incidenthub-worker/Dockerfile -t incidenthub-worker:local .

Write-Host "Tagging images..."
docker tag incidenthub-api:local "${ApiRepository}:${ImageTag}"
docker tag incidenthub-worker:local "${WorkerRepository}:${ImageTag}"

Write-Host "Pushing API image..."
docker push "${ApiRepository}:${ImageTag}"

Write-Host "Pushing Worker image..."
docker push "${WorkerRepository}:${ImageTag}"

Write-Host "Done."
Write-Host "Published images:"
Write-Host "  ${ApiRepository}:${ImageTag}"
Write-Host "  ${WorkerRepository}:${ImageTag}"