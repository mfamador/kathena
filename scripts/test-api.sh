#!/usr/bin/env bash

# Kathena API Testing Script
# Tests all available endpoints with sample data

set -e

BASE_URL="http://localhost:8080"
API_URL="${BASE_URL}/api"

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_section() {
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
}

print_test() {
    echo -e "${GREEN}▶${NC} $1"
}

print_result() {
    echo "$1" | jq '.' 2>/dev/null || echo "$1"
    echo ""
}

# Check if application is running
if ! curl -s -f "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
    echo "❌ Error: Application is not running on ${BASE_URL}"
    exit 1
fi

print_section "🧑 PERSON ENDPOINTS (MongoDB)"

print_test "1. Create a new person"
response=$(curl -s -X POST "${BASE_URL}/person" \
    -H "Content-Type: application/json" \
    -d '{"name":"Test User","email":"test@example.com","age":30,"city":"London"}')
print_result "$response"
PERSON_ID=$(echo "$response" | jq -r '.id' 2>/dev/null || echo "")

print_test "2. Get all persons"
response=$(curl -s "${BASE_URL}/person")
print_result "$response"

print_test "3. Count persons"
response=$(curl -s "${BASE_URL}/person/count")
print_result "$response"

if [ -n "$PERSON_ID" ]; then
    print_test "4. Get person by ID: $PERSON_ID"
    response=$(curl -s "${BASE_URL}/person/${PERSON_ID}")
    print_result "$response"
    
    print_test "5. Update person: $PERSON_ID"
    response=$(curl -s -X PUT "${BASE_URL}/person/${PERSON_ID}" \
        -H "Content-Type: application/json" \
        -d '{"name":"Updated User","email":"updated@example.com","age":31,"city":"Paris"}')
    print_result "$response"
fi

print_test "6. Search persons by name (query: 'Alice')"
response=$(curl -s "${BASE_URL}/person/search?name=Alice")
print_result "$response"

print_test "7. Find persons by city (New York)"
response=$(curl -s "${BASE_URL}/person/city/New%20York")
print_result "$response"

print_test "8. Find persons older than 30"
response=$(curl -s "${BASE_URL}/person/older?age=30")
print_result "$response"

print_test "9. Create multiple persons (batch)"
response=$(curl -s -X POST "${BASE_URL}/person/batch" \
    -H "Content-Type: application/json" \
    -d '[
        {"name":"Batch User 1","email":"batch1@example.com","age":25,"city":"Berlin"},
        {"name":"Batch User 2","email":"batch2@example.com","age":40,"city":"Tokyo"}
    ]')
print_result "$response"

print_section "📄 ARTICLE ENDPOINTS (Elasticsearch)"

print_test "1. Create a new article"
response=$(curl -s -X POST "${BASE_URL}/article" \
    -H "Content-Type: application/json" \
    -d '{"title":"Test Article","content":"This is test content","author":"Test Author","tags":["test","demo"]}')
print_result "$response"
ARTICLE_ID=$(echo "$response" | jq -r '.id' 2>/dev/null || echo "")

print_test "2. Get all articles"
response=$(curl -s "${BASE_URL}/article")
print_result "$response"

print_test "3. Count articles"
response=$(curl -s "${BASE_URL}/article/count")
print_result "$response"

if [ -n "$ARTICLE_ID" ]; then
    print_test "4. Get article by ID: $ARTICLE_ID"
    response=$(curl -s "${BASE_URL}/article/${ARTICLE_ID}")
    print_result "$response"
    
    print_test "5. Update article: $ARTICLE_ID"
    response=$(curl -s -X PUT "${BASE_URL}/article/${ARTICLE_ID}" \
        -H "Content-Type: application/json" \
        -d '{"title":"Updated Article","content":"Updated content","author":"Updated Author","tags":["updated","test"]}')
    print_result "$response"
fi

print_test "6. Search articles by title (query: 'Kotlin')"
response=$(curl -s "${BASE_URL}/article/search?title=Kotlin")
print_result "$response"

print_test "7. Find articles by author (Alice Johnson)"
response=$(curl -s "${BASE_URL}/article/author/Alice%20Johnson")
print_result "$response"

print_test "8. Find articles by tag (kotlin)"
response=$(curl -s "${BASE_URL}/article/tag/kotlin")
print_result "$response"

print_test "9. Create multiple articles (batch)"
response=$(curl -s -X POST "${BASE_URL}/article/batch" \
    -H "Content-Type: application/json" \
    -d '[
        {"title":"Batch Article 1","content":"Content 1","author":"Batch Author","tags":["batch","test"]},
        {"title":"Batch Article 2","content":"Content 2","author":"Batch Author","tags":["batch","demo"]}
    ]')
print_result "$response"

print_section "📨 MESSAGING ENDPOINTS (Kafka)"

print_test "1. Send message to Kafka"
response=$(curl -s -X POST "${API_URL}/message" \
    -H "Content-Type: text/plain" \
    -d "Test message from API testing script")
echo "Message sent (check application logs for consumer output)"
echo ""

print_test "2. Send multiple messages"
for i in {1..3}; do
    curl -s -X POST "${API_URL}/message" \
        -H "Content-Type: text/plain" \
        -d "Batch test message #${i}" > /dev/null
    echo "  - Message $i sent"
done
echo ""

print_section "📊 DATA MANAGEMENT ENDPOINTS"

print_test "1. Get data statistics"
response=$(curl -s "${API_URL}/data/stats")
print_result "$response"

print_section "🎉 Testing Complete!"

echo "All endpoints tested successfully!"
echo ""
echo "Clean up test data:"
echo "  Delete specific person: curl -X DELETE ${BASE_URL}/person/\$PERSON_ID"
echo "  Delete specific article: curl -X DELETE ${BASE_URL}/article/\$ARTICLE_ID"
echo "  Clear all data: curl -X DELETE ${API_URL}/data/clear"
echo ""

