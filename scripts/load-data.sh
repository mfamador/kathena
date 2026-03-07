#!/usr/bin/env bash

# Kathena Data Loader Script
# This script loads sample data into all data stores (MongoDB, Elasticsearch, Kafka)

set -e

BASE_URL="http://localhost:8080"
API_URL="${BASE_URL}/api"

echo "🚀 Kathena Data Loader"
echo "======================"
echo ""

# Check if the application is running
echo "📡 Checking if Kathena is running..."
if ! curl -s -f "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
    echo "❌ Error: Kathena application is not running on ${BASE_URL}"
    echo "   Please start the application first with: ./gradlew bootRun"
    exit 1
fi
echo "✅ Application is running"
echo ""

# Load all data using the data loader endpoint
echo "📊 Loading sample data into all stores..."
response=$(curl -s -X POST "${API_URL}/data/load" \
    -H "Content-Type: application/json")

echo "✅ Data loaded successfully!"
echo "$response" | jq '.' 2>/dev/null || echo "$response"
echo ""

# Display statistics
echo "📈 Current data statistics:"
stats=$(curl -s "${API_URL}/data/stats")
echo "$stats" | jq '.' 2>/dev/null || echo "$stats"
echo ""

echo "🎉 Data loading completed!"
echo ""
echo "Try these commands to verify:"
echo "  curl ${BASE_URL}/person | jq"
echo "  curl ${BASE_URL}/article | jq"
echo "  curl '${BASE_URL}/person/search?name=Alice' | jq"
echo "  curl '${BASE_URL}/article/search?title=Kotlin' | jq"

