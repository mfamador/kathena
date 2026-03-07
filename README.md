# Kathena

Spring Boot + WebFlux playground with Reactive Mongo, Elasticsearch, and Kafka.

A modern reactive microservice demonstrating:
- 🔄 **Reactive Programming** with Spring WebFlux and Project Reactor
- 🍃 **MongoDB** for document storage (Person entities)
- 🔍 **Elasticsearch** for full-text search (Article entities)
- 📨 **Kafka** (Redpanda) for event streaming
- 🎯 **Kotlin** for concise, expressive code

## Prerequisites

- **Java 21** (Temurin/OpenJDK recommended)
- **Docker** (for MongoDB, Elasticsearch, and Redpanda)
- **curl** or **httpie** (for testing APIs)
- **jq** (optional, for pretty JSON output)

Verify your setup:
```bash
java -version    # Should show Java 21
docker --version
```

## Quick Start

### Option 1: Super Quick (Recommended for first-time users)

```bash
./quick-start.sh
```

Then in another terminal:
```bash
./gradlew bootRun
```

### Option 2: Using Makefile (Easiest)

```bash
make dev          # Starts containers and runs the app
```

Or step by step:
```bash
make start        # Start containers
make run          # Run application
make load-data    # Load sample data
make test-api     # Test all endpoints
```

### Option 3: Using Docker Compose

```bash
docker-compose up -d    # Start all containers
./gradlew bootRun       # Run application
```

### Option 4: Using Scripts

```bash
./scripts/containers.sh start  # Start all containers
./gradlew bootRun              # Run application
./scripts/load-data.sh         # Load sample data
```

### Option 5: Manual Docker Commands

```bash
# MongoDB
docker run --name kathena-mongo -p 27017:27017 -d mongo:7

# Elasticsearch
docker run --name kathena-es -p 9200:9200 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -d docker.elastic.co/elasticsearch/elasticsearch:8.15.5

# Redpanda (Kafka-compatible)
docker run --name kathena-redpanda -p 9092:9092 -p 9644:9644 \
  -d redpandadata/redpanda:latest \
  redpanda start --smp 1 --memory 1G --overprovisioned --node-id 0 --check=false \
  --kafka-addr PLAINTEXT://0.0.0.0:9092 --advertise-kafka-addr PLAINTEXT://localhost:9092
```

## Step-by-Step Setup Guide

### 1. Clone and Build

```bash
cd /path/to/kathena
./gradlew build
```

### 2. Verify Containers are Running

Wait a few seconds for services to start, then verify:

```bash
# Check MongoDB
docker logs kathena-mongo

# Check Elasticsearch
curl http://localhost:9200/_cluster/health

# Check Redpanda
docker logs kathena-redpanda
```

All services should be accessible on their respective ports.

### 3. Start the Application

Run from command line:
```bash
./gradlew bootRun
```

Or run from IntelliJ IDEA:
1. Open `src/main/kotlin/com/github/mfamador/kathena/KathenaApp.kt`
2. Click the green run button next to `fun main()`

Wait for the application to start. You should see:
```
Started KathenaAppKt in X.XXX seconds
```

### 4. Verify Application is Running

```bash
curl http://localhost:8080/actuator/health
```

Should return:
```json
{"status":"UP"}
```

### 5. Load Sample Data

Use the data loader script:

```bash
./scripts/load-data.sh
```

Or use the API directly:

```bash
curl -X POST http://localhost:8080/api/data/load
```

This will populate:
- 10 persons in MongoDB
- 10 articles in Elasticsearch
- 10 messages to Kafka

### 6. Test the APIs

Run the automated test script:

```bash
./scripts/test-api.sh
```

Or test manually:

```bash
# Get all persons
curl http://localhost:8080/person | jq

# Search for persons named "Alice"
curl "http://localhost:8080/person/search?name=Alice" | jq

# Get all articles
curl http://localhost:8080/article | jq

# Search articles about "Kotlin"
curl "http://localhost:8080/article/search?title=Kotlin" | jq

# Get statistics
curl http://localhost:8080/api/data/stats | jq
```

### 7. Access Swagger UI

Open your browser to:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/v3/api-docs

## Container Management

The `scripts/containers.sh` script provides easy container management:

```bash
./scripts/containers.sh start      # Start all containers
./scripts/containers.sh stop       # Stop all containers
./scripts/containers.sh restart    # Restart all containers
./scripts/containers.sh remove     # Remove all containers (deletes data)
./scripts/containers.sh status     # Show container status
./scripts/containers.sh logs mongo # View MongoDB logs
./scripts/containers.sh logs kafka # View Kafka logs
```

## API Documentation

Once the application is running, access the API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## API Endpoints

### Person Endpoints (MongoDB - Reactive)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/person` | Get all persons |
| GET | `/person/{id}` | Get person by ID |
| GET | `/person/count` | Count all persons |
| POST | `/person` | Create new person |
| POST | `/person/batch` | Create multiple persons |
| PUT | `/person/{id}` | Update person |
| DELETE | `/person/{id}` | Delete person |
| DELETE | `/person` | Delete all persons |
| GET | `/person/search?name={name}` | Search persons by name |
| GET | `/person/city/{city}` | Find persons by city |
| GET | `/person/older?age={age}` | Find persons older than age |

**Person Model:**
```json
{
  "id": "string",
  "name": "string",
  "email": "string",
  "age": 30,
  "city": "string",
  "createdAt": "2026-03-07T18:00:00"
}
```

### Article Endpoints (Elasticsearch)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/article` | Get all articles |
| GET | `/article/{id}` | Get article by ID |
| GET | `/article/count` | Count all articles |
| POST | `/article` | Create new article |
| POST | `/article/batch` | Create multiple articles |
| PUT | `/article/{id}` | Update article |
| DELETE | `/article/{id}` | Delete article |
| DELETE | `/article` | Delete all articles |
| GET | `/article/search?title={title}` | Search articles by title |
| GET | `/article/author/{author}` | Find articles by author |
| GET | `/article/tag/{tag}` | Find articles by tag |

**Article Model:**
```json
{
  "id": "string",
  "title": "string",
  "content": "string",
  "author": "string",
  "tags": ["tag1", "tag2"],
  "publishedAt": "2026-03-07T18:00:00"
}
```

### Messaging Endpoints (Kafka)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/message` | Send message to Kafka topic |

### Data Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/data/load` | Load sample data into all stores |
| DELETE | `/api/data/clear` | Clear all data from all stores |
| GET | `/api/data/stats` | Get data statistics |

## Example Requests

### Person Examples

```bash
# Create a person
curl -X POST http://localhost:8080/person \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ada Lovelace",
    "email": "ada@example.com",
    "age": 28,
    "city": "London"
  }'

# Search by name
curl "http://localhost:8080/person/search?name=Ada"

# Find by city
curl http://localhost:8080/person/city/London

# Batch create
curl -X POST http://localhost:8080/person/batch \
  -H "Content-Type: application/json" \
  -d '[
    {"name": "User 1", "email": "user1@example.com", "age": 25, "city": "NYC"},
    {"name": "User 2", "email": "user2@example.com", "age": 35, "city": "LA"}
  ]'
```

### Article Examples

```bash
# Create an article
curl -X POST http://localhost:8080/article \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Introduction to Reactive Programming",
    "content": "Reactive programming is a paradigm...",
    "author": "Ada Lovelace",
    "tags": ["reactive", "programming", "tutorial"]
  }'

# Search by title
curl "http://localhost:8080/article/search?title=Reactive"

# Find by author
curl http://localhost:8080/article/author/Ada%20Lovelace

# Find by tag
curl http://localhost:8080/article/tag/kotlin

# Batch create
curl -X POST http://localhost:8080/article/batch \
  -H "Content-Type: application/json" \
  -d '[
    {"title": "Article 1", "content": "Content 1", "author": "Author 1", "tags": ["tag1"]},
    {"title": "Article 2", "content": "Content 2", "author": "Author 2", "tags": ["tag2"]}
  ]'
```

### Messaging Examples

```bash
# Send a message to Kafka
curl -X POST http://localhost:8080/api/message \
  -H "Content-Type: text/plain" \
  -d "Hello from Kathena!"

# Check application logs to see the consumer receiving the message
```

### Data Management Examples

```bash
# Load sample data
curl -X POST http://localhost:8080/api/data/load

# Get statistics
curl http://localhost:8080/api/data/stats

# Clear all data
curl -X DELETE http://localhost:8080/api/data/clear
```

## Testing

### Comprehensive Test Suite

This project includes extensive test coverage:
- ✅ **Model Unit Tests** - Data class validation (no Docker needed)
- ✅ **Repository Integration Tests** - MongoDB & Elasticsearch
- ✅ **Controller Integration Tests** - All REST endpoints
- ✅ **Kafka Integration Tests** - Message streaming
- ✅ **Error Handling Tests** - Edge cases and error scenarios
- ✅ **Actuator Tests** - Health checks and metrics

### Quick Test Commands

**New! Use the test runner script:**

```bash
./scripts/run-tests.sh help        # Show all options
./scripts/run-tests.sh unit        # Fast unit tests (no Docker)
./scripts/run-tests.sh repository  # MongoDB & Elasticsearch tests
./scripts/run-tests.sh controller  # API integration tests
./scripts/run-tests.sh kafka       # Kafka integration tests
./scripts/run-tests.sh all         # Full test suite
```

### Run All Tests

Requires Docker running:

```bash
./gradlew test
```

### Run Specific Test Categories

```bash
# Unit tests only (fast, no Docker needed)
./gradlew test --tests "*.model.*"

# Repository tests (needs MongoDB & Elasticsearch)
./gradlew test --tests "*.repository.*"

# Controller/API tests (needs all containers)
./gradlew test --tests "*.controller.*"

# Kafka tests (needs Redpanda/Kafka)
./gradlew test --tests "*.kafka.*"
```

### Automated API Testing

Run the comprehensive API test script:

```bash
./scripts/test-api.sh
```

This will test all endpoints and display formatted results.

### Manual Testing

Use the provided HTTP file for IntelliJ IDEA:

```bash
src/test/test-rest-endpoints.http
```


## Configuration

Configuration is in `src/main/resources/application.properties`:

```properties
# Server
spring.application.name=kathena
server.port=8080

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/kathena

# Elasticsearch
elasticsearch.host=localhost:9200

# Kafka
kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=kathena-consumer
spring.kafka.consumer.auto-offset-reset=earliest
```

## Project Structure

```
kathena/
├── src/main/kotlin/com/github/mfamador/kathena/
│   ├── KathenaApp.kt              # Application entry point
│   ├── config/                    # Configuration classes
│   │   ├── ElasticsearchConfig.kt
│   │   ├── KafkaConfig.kt
│   │   ├── MongoConfig.kt
│   │   └── OpenApiConfig.kt
│   ├── controller/                # REST controllers
│   │   ├── ArticleController.kt   # Article REST API
│   │   ├── PersonController.kt    # Person REST API
│   │   ├── MessageApi.kt          # Kafka messaging API
│   │   ├── DataLoaderController.kt
│   │   └── RestErrorHandler.kt
│   ├── model/                     # Domain models
│   │   ├── Article.kt
│   │   ├── Person.kt
│   │   └── ApiError.kt
│   ├── repository/                # Data access layer
│   │   ├── ArticleRepository.kt
│   │   └── PersonRepository.kt
│   ├── service/                   # Business logic
│   │   ├── ArticleService.kt
│   │   └── PersonService.kt
│   └── messaging/                 # Kafka producers/consumers
│       ├── EventProducer.kt
│       └── EventConsumer.kt
├── scripts/                       # Utility scripts
│   ├── containers.sh              # Container management
│   ├── load-data.sh               # Data loader
│   ├── test-api.sh                # API testing
│   └── verify-endpoints.sh        # Quick verification
├── docker-compose.yml             # Docker Compose configuration
├── Makefile                       # Task automation
└── build.gradle.kts               # Build configuration
```

## Technology Stack

- **Kotlin** 2.1.10
- **Spring Boot** 3.4.2
- **Spring WebFlux** - Reactive web framework
- **Spring Data MongoDB Reactive** - Reactive MongoDB integration
- **Spring Data Elasticsearch** - Elasticsearch integration
- **Spring Kafka** - Kafka integration
- **MongoDB** 7.x - Document database
- **Elasticsearch** 8.15.5 - Search engine
- **Redpanda** - Kafka-compatible streaming platform
- **Gradle** 8.12 - Build tool

## Development

### IntelliJ IDEA Setup

#### Run from IDE

1. Open the project in IntelliJ IDEA
2. Wait for Gradle sync to complete
3. Make sure Project SDK is set to Java 21:
   - File → Project Structure → Project → SDK
4. Run the main application:
   - Open `src/main/kotlin/com/github/mfamador/kathena/KathenaApp.kt`
   - Click the green run button next to `fun main()`

#### Use HTTP Client

IntelliJ has a built-in HTTP client:

1. Open `src/test/test-rest-endpoints.http`
2. Click the run button next to any request
3. View results in the Run window

### Development Tips

#### Hot Reload

The project includes Spring Boot DevTools, which enables hot reload:
- Make changes to Kotlin files
- Application will automatically restart

#### Debug in IntelliJ

1. Click the debug button (instead of run) next to `fun main()`
2. Set breakpoints in your code
3. Debug requests using the HTTP client

#### Monitor Logs

View real-time logs:
```bash
./gradlew bootRun | grep "com.github.mfamador.kathena"
```

#### Direct Database Access

```bash
# MongoDB
docker exec -it kathena-mongo mongosh kathena

# Elasticsearch
curl "http://localhost:9200/blog/_search?pretty"
```

### Build the project

```bash
./gradlew build
```

### Run tests

```bash
./gradlew test
```

### Check for dependency updates

```bash
./gradlew dependencyUpdates
```

### Build Docker image (if configured)

```bash
./gradlew bootBuildImage
```

## Troubleshooting

### Port Already in Use

If you get "Port already in use" errors:

```bash
# Find what's using the port
lsof -i :27017  # MongoDB
lsof -i :9200   # Elasticsearch
lsof -i :9092   # Kafka
lsof -i :8080   # Application

# Kill the process if needed, or change the port in application.properties
```

### Containers Won't Start

```bash
# Check Docker is running
docker ps

# View container logs
./scripts/containers.sh logs mongo
./scripts/containers.sh logs es
./scripts/containers.sh logs kafka

# Or with docker-compose
docker-compose logs mongodb
docker-compose logs elasticsearch
```

### Connection Refused Errors

Make sure all containers are running and healthy:

```bash
# Check container status
./scripts/containers.sh status

# Wait for containers to be fully ready (especially Elasticsearch)
sleep 10

# Test connectivity
curl http://localhost:9200     # Elasticsearch should respond
docker exec kathena-mongo mongosh --eval "db.runCommand('ping')"
```

### Application Won't Start

1. Check that all containers are running:
   ```bash
   ./scripts/containers.sh status
   ```

2. Check logs for specific error messages

3. Verify Java 21 is being used:
   ```bash
   ./gradlew --version
   java -version
   ```

4. Make sure port 8080 is not in use:
   ```bash
   lsof -i :8080
   ```

### Bean Name Conflicts

If you see "bean name already defined" errors, this has been fixed by renaming the router beans to `personRoute()` and `articleRoute()`.

### Clear Everything and Start Fresh

```bash
# Remove all containers and data
./scripts/containers.sh remove

# Or with docker-compose
docker-compose down -v

# Clean build
./gradlew clean

# Start fresh
./scripts/containers.sh start
./gradlew bootRun
```

### Elasticsearch Not Ready

Elasticsearch can take 10-15 seconds to start. If you see connection errors:

```bash
# Check Elasticsearch health
curl http://localhost:9200/_cluster/health

# Wait for green or yellow status
# Green = ready, Yellow = functional but not optimal, Red = problem
```

### Kafka Consumer Issues

If Kafka messages aren't being consumed:

```bash
# Check Redpanda logs
./scripts/containers.sh logs kafka

# Verify topic exists
docker exec kathena-redpanda rpk topic list

# Check consumer group
docker exec kathena-redpanda rpk group describe kathena-consumer
```

## Useful Commands

```bash
# Container management
./scripts/containers.sh start      # Start all containers
./scripts/containers.sh stop       # Stop all containers
./scripts/containers.sh status     # Check status
./scripts/containers.sh logs mongo # View MongoDB logs

# Data management
./scripts/load-data.sh             # Load sample data
curl -X POST http://localhost:8080/api/data/load  # Load via API
curl -X DELETE http://localhost:8080/api/data/clear  # Clear all data
curl http://localhost:8080/api/data/stats | jq     # View statistics

# API testing
./scripts/test-api.sh              # Run all API tests
./scripts/verify-endpoints.sh      # Quick verification

# Build and run
./gradlew build                    # Build project
./gradlew test                     # Run tests
./gradlew bootRun                  # Run application
./gradlew clean build              # Clean and rebuild
./gradlew dependencyUpdates        # Check for dependency updates

# Make shortcuts (if using Makefile)
make help                          # Show all available commands
make dev                           # Complete setup and run
make start                         # Start containers
make run                           # Run application
make load-data                     # Load sample data
make test-api                      # Test all endpoints
make swagger                       # Open Swagger UI
```

## Learning Resources

This project demonstrates:
- **Reactive Programming**: Non-blocking I/O with Project Reactor
- **Spring WebFlux**: Reactive web framework with annotated controllers
- **REST Controllers**: @RestController with full OpenAPI documentation
- **MongoDB Reactive**: Reactive CRUD operations with ReactiveCrudRepository
- **Elasticsearch**: Full-text search capabilities with query methods
- **Kafka Integration**: Event-driven messaging with producers and consumers
- **Kotlin Features**: Data classes, extension functions, coroutines integration

## Architecture Decision Records

Key architectural decisions are documented in [docs/adr/](docs/adr/README.md):

- [ADR-0001: Use Spring WebFlux for Reactive Programming](docs/adr/0001-use-spring-webflux-for-reactive-programming.md)
- [ADR-0002: Use MongoDB for Document Storage](docs/adr/0002-use-mongodb-for-document-storage.md)
- [ADR-0003: Use Elasticsearch for Full-Text Search](docs/adr/0003-use-elasticsearch-for-full-text-search.md)
- [ADR-0004: Use Apache Kafka for Event Streaming](docs/adr/0004-use-kafka-for-event-streaming.md)
- [ADR-0005: Use Kotlin as Primary Language](docs/adr/0005-use-kotlin-as-primary-language.md)
- [ADR-0006: Use Functional Router Pattern for HTTP Endpoints](docs/adr/0006-use-functional-router-pattern-for-http-endpoints.md)
- [ADR-0007: Use Docker Compose for Integration Tests](docs/adr/0007-use-docker-compose-for-integration-tests.md)
- [ADR-0008: Implement Custom Migration System](docs/adr/0008-implement-custom-migration-system.md)
- [ADR-0009: Use Redpanda Instead of Apache Kafka in Development](docs/adr/0009-use-redpanda-instead-of-kafka-in-development.md)
- [ADR-0010: Use ktlint and detekt for Code Quality](docs/adr/0010-use-ktlint-and-detekt-for-code-quality.md)

## Cleanup

Stop and remove all containers:

```bash
./scripts/containers.sh remove
```

Or manually:

```bash
docker rm -f kathena-mongo kathena-es kathena-redpanda
```

## License

This is a playground project for learning purposes.


