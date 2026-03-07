# 9. Use Redpanda Instead of Apache Kafka in Development

Date: 2026-03-07

## Status

Accepted

## Context

Apache Kafka requires ZooKeeper and has complex configuration for local development. Developers need a simple, fast Kafka-compatible message broker for local testing without the operational overhead of a full Kafka cluster.

## Decision

We will use Redpanda in development and test environments as a drop-in replacement for Apache Kafka, while supporting Kafka in production environments.

## Consequences

### Positive

- Single binary, no ZooKeeper dependency
- Faster startup times (seconds vs minutes)
- Lower resource consumption (less memory/CPU)
- 100% Kafka API compatible
- Simpler configuration for development
- Built-in REST admin API
- Better developer experience

### Negative

- Different internal architecture than Apache Kafka
- Some advanced Kafka features may behave differently
- Production environments still use Kafka (separate testing needed)
- Smaller community compared to Apache Kafka
- Potential subtle behavioral differences in edge cases

## Implementation Notes

- Redpanda runs in Docker Compose for tests and local dev
- Same Kafka client libraries work with both Redpanda and Kafka
- Configuration uses `bootstrap-servers` property (same for both)
- Health checks via Redpanda's `/v1/status/ready` endpoint
- Port 19092 for local Redpanda (9092 reserved for production Kafka)

