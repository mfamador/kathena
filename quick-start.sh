#!/usr/bin/env bash

# Kathena Quick Start Script
# Sets up everything and runs the application

set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}   Kathena Quick Start${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# Step 1: Check prerequisites
echo -e "${GREEN}Step 1: Checking prerequisites...${NC}"
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 21."
    exit 1
fi
echo "✅ Java found: $(java -version 2>&1 | head -n 1)"

if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Please install Docker."
    exit 1
fi
echo "✅ Docker found: $(docker --version)"
echo ""

# Step 2: Start containers
echo -e "${GREEN}Step 2: Starting Docker containers...${NC}"
./scripts/containers.sh start
echo ""

# Step 3: Wait for services
echo -e "${GREEN}Step 3: Waiting for services to be ready...${NC}"
echo "This may take 10-15 seconds..."
sleep 10

# Check services
echo -e "${YELLOW}Checking MongoDB...${NC}"
if docker exec kathena-mongo mongosh --eval "db.runCommand('ping')" --quiet 2>/dev/null; then
    echo "✅ MongoDB is ready"
else
    echo "⚠️  MongoDB might still be starting..."
fi

echo -e "${YELLOW}Checking Elasticsearch...${NC}"
if curl -s -f http://localhost:9200/_cluster/health > /dev/null 2>&1; then
    echo "✅ Elasticsearch is ready"
else
    echo "⚠️  Elasticsearch might still be starting..."
fi

echo -e "${YELLOW}Checking Redpanda...${NC}"
if docker logs kathena-redpanda 2>&1 | grep -q "Successfully started Redpanda"; then
    echo "✅ Redpanda is ready"
else
    echo "⚠️  Redpanda might still be starting..."
fi
echo ""

# Step 4: Build the application
echo -e "${GREEN}Step 4: Building the application...${NC}"
./gradlew build -q --console=plain
echo "✅ Build completed"
echo ""

# Step 5: Show next steps
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✨ Setup Complete!${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "Next steps:"
echo ""
echo "1. Start the application:"
echo -e "   ${YELLOW}./gradlew bootRun${NC}"
echo ""
echo "2. In another terminal, load sample data:"
echo -e "   ${YELLOW}./scripts/load-data.sh${NC}"
echo ""
echo "3. Test the APIs:"
echo -e "   ${YELLOW}./scripts/test-api.sh${NC}"
echo ""
echo "4. Access Swagger UI:"
echo -e "   ${YELLOW}http://localhost:8080/swagger-ui.html${NC}"
echo ""
echo "5. View container status:"
echo -e "   ${YELLOW}./scripts/containers.sh status${NC}"
echo ""
echo "Quick commands:"
echo "  make dev          # Start containers and run app"
echo "  make load-data    # Load sample data"
echo "  make test-api     # Test all endpoints"
echo "  make swagger      # Open Swagger UI"
echo "  make help         # Show all available commands"
echo ""

