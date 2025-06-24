# Set up log files with timestamps
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$logDir = "logs"
$mvnLog = "$logDir/mvn-build-$timestamp.log"
$dockerLog = "$logDir/docker-build-$timestamp.log"

# Create log directory if missing
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}

# Step 1: Maven Build
Write-Output "`n[STEP 1] Running Maven build..."
$mvnOutput = mvn clean package -DskipTests *>&1
$mvnOutput | Out-File -FilePath $mvnLog -Encoding UTF8

if ($LASTEXITCODE -ne 0) {
    Write-Output "`n[ERROR] Maven build failed. Showing partial log:"
    $mvnOutput | Select-Object -First 30
    Write-Output "... (full log at $mvnLog)"
    exit 1
}

Write-Output "[OK] Maven build completed."

# Step 2: Rebuild and Restart Specific Docker Services
# Define the microservices you want to rebuild/restart
$servicesToRebuild = @(
    "discovery-server",
    "api-gateway",
    "product-service",
    "order-service",
    "inventory-service",
    "notification-service"
)

Write-Output "`n[STEP 2] Rebuilding and restarting selected Docker services..."
foreach ($service in $servicesToRebuild) {
    Write-Output "`n[INFO] Rebuilding $service ..."
    $buildOutput = docker-compose build $service *>&1
    $buildOutput | Out-File -Append -FilePath $dockerLog -Encoding UTF8

    if ($LASTEXITCODE -ne 0) {
        Write-Output "[ERROR] Failed to build $service. Check logs for details."
        $buildOutput | Select-Object -First 20
        exit 1
    }

    Write-Output "[INFO] Restarting $service ..."
    $upOutput = docker-compose up -d --no-deps --force-recreate $service *>&1
    $upOutput | Out-File -Append -FilePath $dockerLog -Encoding UTF8

    if ($LASTEXITCODE -ne 0) {
        Write-Output "[ERROR] Failed to restart $service. Check logs for details."
        $upOutput | Select-Object -First 20
        exit 1
    }

    Write-Output "[OK] $service restarted successfully."
}

# Final message
Write-Output "`n[SUCCESS] Selected services rebuilt and deployed successfully."
Write-Output "Logs saved to: $mvnLog and $dockerLog"
