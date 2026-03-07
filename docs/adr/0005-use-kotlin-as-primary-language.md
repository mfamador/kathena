# 5. Use Kotlin as Primary Language

Date: 2026-03-07

## Status

Accepted

## Context

We need a modern, type-safe JVM language that provides good developer experience, excellent IDE support, and seamless interoperability with the Spring ecosystem.

## Decision

We will use Kotlin as the primary programming language for this project, targeting Kotlin 2.1.x (latest stable) with JVM target 21.

## Consequences

### Positive

- Concise syntax reduces boilerplate compared to Java
- Null safety prevents common runtime errors
- Coroutines provide elegant async/concurrent programming
- Excellent Spring Boot integration via Spring Kotlin support
- Data classes eliminate boilerplate for DTOs/models
- Extension functions enable expressive APIs
- Growing adoption in Spring community
- First-class IDE support in IntelliJ IDEA

### Negative

- Requires team familiarity with Kotlin-specific idioms
- Slightly longer compilation times than Java
- Smaller talent pool compared to Java
- Some Java libraries may have limited Kotlin-specific documentation

## Implementation Notes

- Using Kotlin 2.1.10 (latest stable)
- Kotlin reflection for Spring framework features
- Jackson Kotlin module for JSON serialization
- Kotlin coroutines for reactive interop where beneficial
- ktlint and detekt for code quality and consistency

