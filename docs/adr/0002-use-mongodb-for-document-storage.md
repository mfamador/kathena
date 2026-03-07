# 2. Use MongoDB for Document Storage

Date: 2026-03-07

## Status

Accepted

## Context

We need a database to store user/person data with flexible schema and support for reactive operations. The data model is primarily document-based with occasional queries by fields like name, email, city, and age.

## Decision

We will use MongoDB as our primary document database with Spring Data MongoDB Reactive for non-blocking database operations.

## Consequences

### Positive

- Schema flexibility allows easy evolution of the `Person` model
- Native support for reactive streams via `ReactiveMongoRepository`
- Rich query capabilities with Spring Data MongoDB
- Horizontal scalability through sharding
- Good fit for document-based data structures
- Excellent community support and tooling

### Negative

- Eventual consistency model requires careful transaction handling
- Complex joins are not idiomatic (denormalization preferred)
- Index management requires attention to query patterns
- Storage overhead compared to columnar databases

## Implementation Notes

- Using reactive repository pattern for non-blocking operations
- Custom indexes on email (unique), city+age (compound)
- Migration system to manage schema changes

