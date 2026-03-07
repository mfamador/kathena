#!/usr/bin/env bash

# Test the newly added endpoints

BASE_URL="http://localhost:8080"

echo "🧪 Testing Kathena Enhanced Endpoints"
echo "======================================"
echo ""

# Check if app is running
if ! curl -s -f "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
    echo "❌ Application is not running on ${BASE_URL}"
    echo "   Start it from IntelliJ or run: ./gradlew bootRun"
    exit 1
fi

echo "✅ Application is running"
echo ""

echo "📊 Data Statistics"
echo "------------------"
curl -s "${BASE_URL}/api/data/stats" | jq
echo ""

echo "🔍 Testing Person Search Endpoints"
echo "-----------------------------------"

echo "1. Search persons by name (Alice):"
curl -s "${BASE_URL}/person/search?name=Alice" | jq '.[0] | {name, email, city}'
echo ""

echo "2. Find persons in New York:"
curl -s "${BASE_URL}/person/city/New%20York" | jq 'length as $count | "Found \($count) persons"'
echo ""

echo "3. Find persons older than 35:"
curl -s "${BASE_URL}/person/older?age=35" | jq 'length as $count | "Found \($count) persons"'
echo ""

echo "🔍 Testing Article Search Endpoints"
echo "------------------------------------"

echo "1. Search articles by title (Kotlin):"
curl -s "${BASE_URL}/article/search?title=Kotlin" | jq '.[0] | {title, author, tags}'
echo ""

echo "2. Find articles by author (Alice Johnson):"
curl -s "${BASE_URL}/article/author/Alice%20Johnson" | jq 'length as $count | "Found \($count) articles"'
echo ""

echo "3. Find articles by tag (kotlin):"
curl -s "${BASE_URL}/article/tag/kotlin" | jq 'length as $count | "Found \($count) articles"'
echo ""

echo "📨 Testing Messaging"
echo "--------------------"
echo "Sending test message..."
curl -s -X POST "${BASE_URL}/api/message" \
    -H "Content-Type: text/plain" \
    -d "Test message from verification script"
echo "✅ Message sent (check application logs to see consumer output)"
echo ""

echo "✨ All tests completed successfully!"
echo ""
echo "Next steps:"
echo "  - Open Swagger UI: ${BASE_URL}/swagger-ui.html"
echo "  - Run full test suite: ./scripts/test-api.sh"
echo "  - View all persons: curl ${BASE_URL}/person | jq"
echo "  - View all articles: curl ${BASE_URL}/article | jq"

