# ==============================================================================
# Learnify EduAI Backend - Google Cloud Run Windows PowerShell Deployment Script
# ==============================================================================

param(
    [string]$ServiceName = "learnify-ai-backend",
    [string]$Region = "us-central1"
)

$ErrorActionPreference = "Stop"

Write-Host "=== Learnify AI Backend Deployment ===" -ForegroundColor Cyan

# Check gcloud CLI
if (-not (Get-Command gcloud -ErrorAction SilentlyContinue)) {
    Write-Error "ERROR: gcloud CLI is not installed or not in PATH. Download from https://cloud.google.com/sdk/docs/install"
    exit 1
}

$ProjectId = (gcloud config get-value project 2>$null).Trim()
if ([string]::IsNullOrWhiteSpace($ProjectId)) {
    Write-Error "ERROR: No default Google Cloud project set. Run: gcloud config set project YOUR_PROJECT_ID"
    exit 1
}

Write-Host "Project ID: $ProjectId"
Write-Host "Service:    $ServiceName"
Write-Host "Region:     $Region"

# Check API Key
$ApiKey = $env:GEMINI_API_KEY
if ([string]::IsNullOrWhiteSpace($ApiKey)) {
    $SecureKey = Read-Host -Prompt "Enter your Google Gemini API Key (hidden)" -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecureKey)
    $ApiKey = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
}

Write-Host "Deploying container to Google Cloud Run (free-tier config)..." -ForegroundColor Yellow

gcloud run deploy $ServiceName `
    --source . `
    --region $Region `
    --platform managed `
    --allow-unauthenticated `
    --memory 512Mi `
    --cpu 1 `
    --min-instances 0 `
    --max-instances 2 `
    --set-env-vars "GEMINI_MODEL=gemini-flash-lite-latest,GEMINI_API_KEY=$ApiKey" `
    --port 8080

$ServiceUrl = (gcloud run services describe $ServiceName --region $Region --format 'value(status.url)').Trim()

Write-Host ""
Write-Host "=== DEPLOYMENT SUCCESSFUL ===" -ForegroundColor Green
Write-Host "Production HTTPS URL: $ServiceUrl"
Write-Host "Health Endpoint:      $ServiceUrl/api/health"
Write-Host "Status Endpoint:      $ServiceUrl/api/status"
Write-Host ""
Write-Host "To link in Android app/build.gradle, set:"
Write-Host "LEARNIFY_API_BASE_URL=`"$ServiceUrl/`""

