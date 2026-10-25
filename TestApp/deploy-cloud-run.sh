#!/usr/bin/env bash
# ==============================================================================
# Learnify EduAI Backend - Google Cloud Run Zero-Cost Deployment Script
# ==============================================================================
# Prerequisites:
# 1. Google Cloud SDK installed (`gcloud auth login`)
# 2. Google Cloud project created with Cloud Run API enabled
# 3. GEMINI_API_KEY from Google AI Studio (free-tier tier)
# ==============================================================================

set -euo pipefail

SERVICE_NAME="learnify-ai-backend"
REGION="us-central1"

echo "=== Learnify AI Backend Deployment ==="

# Check gcloud CLI
if ! command -v gcloud &> /dev/null; then
    echo "ERROR: gcloud CLI is not installed or not in PATH."
    echo "Download from https://cloud.google.com/sdk/docs/install"
    exit 1
fi

PROJECT_ID=$(gcloud config get-value project 2>/dev/null || true)
if [ -z "$PROJECT_ID" ]; then
    echo "ERROR: No default Google Cloud project set. Run: gcloud config set project YOUR_PROJECT_ID"
    exit 1
fi

echo "Project ID: $PROJECT_ID"
echo "Service:    $SERVICE_NAME"
echo "Region:     $REGION"

# Verify GEMINI_API_KEY
if [ -z "${GEMINI_API_KEY:-}" ]; then
    echo ""
    echo "WARNING: GEMINI_API_KEY is not set in your current shell environment."
    read -rsp "Enter your Google Gemini API Key (input hidden): " GEMINI_API_KEY
    echo ""
fi

# Deploy directly via source build (Cloud Build -> Cloud Run)
echo "Deploying container to Cloud Run..."
gcloud run deploy "$SERVICE_NAME" \
    --source . \
    --region "$REGION" \
    --platform managed \
    --allow-unauthenticated \
    --memory 512Mi \
    --cpu 1 \
    --min-instances 0 \
    --max-instances 2 \
    --set-env-vars "GEMINI_MODEL=gemini-flash-lite-latest,GEMINI_API_KEY=$GEMINI_API_KEY" \
    --port 8080

SERVICE_URL=$(gcloud run services describe "$SERVICE_NAME" --region "$REGION" --format 'value(status.url)')

echo ""
echo "=== DEPLOYMENT SUCCESSFUL ==="
echo "Production HTTPS URL: $SERVICE_URL"
echo "Health Endpoint:      $SERVICE_URL/api/health"
echo "Status Endpoint:      $SERVICE_URL/api/status"
echo ""
echo "Next step: In TestApp/app/build.gradle, set:"
echo "LEARNIFY_API_BASE_URL=\"$SERVICE_URL/\""

