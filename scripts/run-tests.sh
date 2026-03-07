#!/bin/bash

# Kathena Test Runner
# Runs different categories of tests

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored messages
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_message "$RED" "❌ Error: Docker is not running."
        print_message "$YELLOW" "   Please start Docker Desktop and try again."
        exit 1
    fi
    print_message "$GREEN" "✅ Docker is running"
}

# Parse command line arguments
TEST_TYPE="${1:-all}"

case "$TEST_TYPE" in
    "unit")
        print_message "$BLUE" "🧪 Running Unit Tests (No Docker needed)..."
        print_message "$YELLOW" "   Testing: Model classes (Person, Article)"
        ./gradlew test --tests "*.model.*" --console=plain
        ;;
    
    "repository")
        print_message "$BLUE" "🗄️  Running Repository Tests..."
        check_docker
        print_message "$YELLOW" "   This will start MongoDB and Elasticsearch containers"
        print_message "$YELLOW" "   First run may take 2-3 minutes..."
        ./gradlew test --tests "*.repository.*" --console=plain
        ;;
    
    "controller")
        print_message "$BLUE" "🌐 Running Controller Tests..."
        check_docker
        print_message "$YELLOW" "   This will start all containers (MongoDB, Elasticsearch, Kafka)"
        print_message "$YELLOW" "   First run may take 2-3 minutes..."
        ./gradlew test --tests "*.controller.*" --console=plain
        ;;
    
    "kafka")
        print_message "$BLUE" "📨 Running Kafka Tests..."
        check_docker
        print_message "$YELLOW" "   This will start Kafka (Redpanda) container"
        ./gradlew test --tests "*.kafka.*" --console=plain
        ;;
    
    "integration")
        print_message "$BLUE" "🔗 Running All Integration Tests..."
        check_docker
        print_message "$YELLOW" "   This will start all containers"
        print_message "$YELLOW" "   First run may take 2-3 minutes..."
        ./gradlew test --tests "*.repository.*" --tests "*.controller.*" --tests "*.kafka.*" --console=plain
        ;;
    
    "all")
        print_message "$BLUE" "🚀 Running All Tests..."
        check_docker
        print_message "$YELLOW" "   This will start all containers"
        print_message "$YELLOW" "   First run may take 2-3 minutes..."
        ./gradlew test --console=plain
        ;;
    
    "quick")
        print_message "$BLUE" "⚡ Running Quick Tests (Unit only)..."
        ./gradlew test --tests "*.model.*" --console=plain
        ;;
    
    "help"|*)
        echo "Kathena Test Runner"
        echo ""
        echo "Usage: $0 [test-type]"
        echo ""
        echo "Test Types:"
        echo "  unit         - Run unit tests only (no Docker needed, fast)"
        echo "  repository   - Run repository integration tests"
        echo "  controller   - Run controller/API integration tests"
        echo "  kafka        - Run Kafka integration tests"
        echo "  integration  - Run all integration tests"
        echo "  all          - Run all tests (default)"
        echo "  quick        - Run quick tests (alias for unit)"
        echo "  help         - Show this help message"
        echo ""
        echo "Examples:"
        echo "  $0 unit          # Fast, no Docker"
        echo "  $0 repository    # Test MongoDB and Elasticsearch"
        echo "  $0 all           # Full test suite"
        exit 0
        ;;
esac

# Print results
if [ $? -eq 0 ]; then
    print_message "$GREEN" "✅ All tests passed!"
    print_message "$BLUE" "📊 See full report: build/reports/tests/test/index.html"
else
    print_message "$RED" "❌ Some tests failed"
    print_message "$YELLOW" "📊 See report: build/reports/tests/test/index.html"
    exit 1
fi

