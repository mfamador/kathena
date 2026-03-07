# 3. Use Elasticsearch for Full-Text Search

Date: 2026-03-07

## Status

Accepted

## Context

We need powerful full-text search capabilities for article content and titles. Users should be able to search articles by keywords, authors, and tags with relevance scoring and fast response times.

## Decision

We will use Elasticsearch as our search engine with Spring Data Elasticsearch for integration.

## Consequences

### Positive

- Industry-leading full-text search with relevance scoring
- Advanced query capabilities (fuzzy, phrase, wildcard, etc.)
- Fast search performance even on large datasets
- Built-in analyzers for multiple languages
- Aggregation support for analytics
- Spring Data integration provides familiar repository pattern

### Negative

- Additional infrastructure complexity (another service to manage)
- Eventual consistency between MongoDB and Elasticsearch
- Memory-intensive for large datasets
- Requires careful index mapping design
- Version compatibility between Spring Data Elasticsearch and Elasticsearch server

## Implementation Notes

- Articles stored in `blog` index
- Text fields for title and content with full-text analysis
- Keyword fields for author and tags for exact matching
- Index and mapping created via migrations on startup
- Using blocking repository (non-reactive) for simplicity

