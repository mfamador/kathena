# 6. Use Functional Router Pattern for HTTP Endpoints

Date: 2026-03-07

## Status

Accepted

## Context

Spring WebFlux supports both annotated controllers (`@RestController`) and functional routing DSL. We need to choose an approach for defining our HTTP endpoints that aligns with reactive principles and provides good testability.

## Decision

We will use the functional routing pattern with separate `Handler` and `Router` components for defining HTTP endpoints, while keeping Swagger/OpenAPI support via SpringDoc.

## Consequences

### Positive

- More explicit and type-safe routing definitions
- Better alignment with functional reactive paradigms
- Clear separation between routing logic and business logic
- Easier to test handlers independently
- Compact route definitions in one place
- Performance benefits (no reflection-based scanning)

### Negative

- Less familiar to developers coming from Spring MVC background
- OpenAPI/Swagger integration requires explicit router function configuration
- Slightly more boilerplate (separate Handler and Router classes)
- IDE support for navigation is less mature than annotated controllers

## Implementation Notes

- One `Router` class per domain entity (PersonRouter, ArticleRouter, etc.)
- Corresponding `Handler` classes contain request/response logic
- SpringDoc OpenAPI configured with `RouterFunctionProvider` beans
- Each router gets unique bean name to avoid conflicts
- Consistent error handling via functional exception handlers

