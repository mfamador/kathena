# 7. Use Docker Compose for Integration Tests

Date: 2026-03-07

## Status

Accepted

## Context

Integration tests require real instances of MongoDB, Elasticsearch, and Kafka. We need a reliable, reproducible way to spin up these dependencies for testing without requiring manual infrastructure setup.

## Decision

We will use Docker Compose with the Palantir docker-compose-rule library to manage test containers, spinning up real service instances during test execution.

## Consequences

### Positive

- Tests run against real services, not mocks or in-memory alternatives
- High confidence that tests reflect production behavior
- Consistent test environment across developers and CI/CD
- No manual infrastructure setup required
- Services automatically cleaned up after tests
- Port mapping handled automatically to avoid conflicts

### Negative

- Slower test execution compared to in-memory alternatives
- Requires Docker installed on developer machines and CI
- Increased resource consumption during testing
- Flakiness if services don't start in time (mitigated by health checks)
- Docker socket access required (can be problematic in some CI environments)

## Implementation Notes

- docker-compose-rule manages container lifecycle
- Health checks ensure services are ready before tests run
- Graceful shutdown prevents resource leaks
- Test properties override connection URLs with dynamic ports
- Redpanda used instead of full Kafka for faster startup

