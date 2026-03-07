# 8. Implement Custom Migration System

Date: 2026-03-07

## Status

Accepted

## Context

We need to manage schema changes and index creation for MongoDB and Elasticsearch across different environments. Traditional JVM migration tools (Liquibase, Flyway) don't support Elasticsearch and have limited reactive support.

## Decision

We will implement a custom lightweight migration system using Spring's `ApplicationRunner` to apply idempotent database and search engine schema changes on application startup.

## Consequences

### Positive

- Unified approach for MongoDB indexes and Elasticsearch mappings
- Idempotent migrations prevent duplicate execution
- Simple Kotlin DSL for defining migrations
- Reactive-friendly using Reactor operators
- No external dependencies or migration formats
- Migration history tracked in MongoDB
- Easy to understand and customize

### Negative

- Custom code requires maintenance and testing
- No rollback mechanism (migrations are forward-only)
- Limited validation compared to mature tools
- Migration order must be carefully managed
- No built-in diffing or verification tools

## Implementation Notes

- Migrations stored as Kotlin lambdas in ordered list
- Migration IDs tracked in `schema_migrations` collection
- Each migration is atomic (runs entirely or not at all)
- Elasticsearch operations run on bounded elastic scheduler
- De-duplication logic runs before unique index creation
- Migrations execute on application startup before traffic

