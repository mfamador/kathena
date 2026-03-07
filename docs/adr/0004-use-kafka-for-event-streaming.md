# 4. Use Apache Kafka for Event Streaming

Date: 2026-03-07

## Status

Accepted

## Context

We need an event-driven architecture to decouple components and enable asynchronous processing of domain events like user registrations, article publications, and system events. The system should support high-throughput event streaming with durability guarantees.

## Decision

We will use Apache Kafka (via Redpanda in development) as our event streaming platform with Spring Kafka for integration.

## Consequences

### Positive

- Decouples producers and consumers for better scalability
- Durable event log enables replay and audit capabilities
- High throughput and low latency for event processing
- Natural fit for event sourcing patterns
- Supports multiple consumers per topic
- Spring Kafka provides excellent integration with Spring ecosystem

### Negative

- Additional operational complexity (broker management)
- Learning curve for Kafka concepts (topics, partitions, consumer groups)
- Eventual consistency between services
- Requires monitoring and alerting for broker health
- Network overhead for inter-service communication

## Implementation Notes

- Using `kathena-events` topic for all domain events
- Redpanda in development (Kafka-compatible, simpler setup)
- Consumer group `kathena-consumer` for event processing
- Events logged for observability
- Non-fatal configuration when topics are missing during startup

