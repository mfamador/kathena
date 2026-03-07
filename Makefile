.PHONY: help start stop restart status logs clean build test run load-data test-api

help: ## Show this help message
	@echo 'Kathena - Kotlin Spring Boot WebFlux Playground'
	@echo ''
	@echo 'Usage:'
	@echo '  make <target>'
	@echo ''
	@echo 'Targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# Container management
start: ## Start all Docker containers (MongoDB, Elasticsearch, Kafka)
	@./scripts/containers.sh start

stop: ## Stop all Docker containers
	@./scripts/containers.sh stop

restart: ## Restart all Docker containers
	@./scripts/containers.sh restart

status: ## Show status of all containers
	@./scripts/containers.sh status

logs-mongo: ## Show MongoDB logs
	@./scripts/containers.sh logs mongo

logs-es: ## Show Elasticsearch logs
	@./scripts/containers.sh logs es

logs-kafka: ## Show Kafka logs
	@./scripts/containers.sh logs kafka

clean: ## Remove all containers and volumes
	@./scripts/containers.sh remove

# Docker Compose alternatives
compose-up: ## Start all services using docker-compose
	@docker-compose up -d

compose-down: ## Stop all services using docker-compose
	@docker-compose down

compose-logs: ## Show docker-compose logs
	@docker-compose logs -f

# Application
build: ## Build the application
	@./gradlew build

test: ## Run tests
	@./gradlew test

run: ## Run the application
	@./gradlew bootRun

clean-build: ## Clean and rebuild
	@./gradlew clean build

# Data management
load-data: ## Load sample data into all stores
	@./scripts/load-data.sh

test-api: ## Run API tests
	@./scripts/test-api.sh

clear-data: ## Clear all data from all stores
	@curl -X DELETE http://localhost:8080/api/data/clear

stats: ## Show data statistics
	@curl -s http://localhost:8080/api/data/stats | jq

# Quick setup
setup: start ## Complete setup: start containers and wait for readiness
	@echo "Waiting for services to be ready..."
	@sleep 10
	@echo "✅ Services are ready!"
	@./scripts/containers.sh status

dev: setup run ## Development mode: setup everything and run the app

# Full workflow
all: clean-build setup run ## Clean build, setup containers, and run

# Health checks
health: ## Check application health
	@curl -s http://localhost:8080/actuator/health | jq

check-mongo: ## Check MongoDB connection
	@docker exec kathena-mongo mongosh --eval "db.runCommand('ping')" --quiet

check-es: ## Check Elasticsearch health
	@curl -s http://localhost:9200/_cluster/health | jq

check-all: status health check-mongo check-es ## Check all services

# Documentation
swagger: ## Open Swagger UI in browser
	@open http://localhost:8080/swagger-ui.html || xdg-open http://localhost:8080/swagger-ui.html 2>/dev/null || echo "Open http://localhost:8080/swagger-ui.html in your browser"

# Dependency management
deps-update: ## Check for dependency updates
	@./gradlew dependencyUpdates

deps-report: ## Generate dependency report
	@./gradlew dependencyUpdates -Drevision=release
	@cat build/dependencyUpdates/report.txt

