# ADR-002 — Technology stack

**Status:** Accepted

## Context
Brief mandates Java. Small team, AI-assisted development, production intent, modest scale.

## Decision
| Choice | Version | Why |
|---|---|---|
| Java | 21 (LTS) | records, sealed types, pattern matching, virtual threads available; LTS to 2029 |
| Spring Boot | 3.3.x | current stable on Java 21; huge training corpus ⇒ AI agents write it correctly |
| Spring Web MVC | — | not WebFlux: our workload is DB-bound CRUD, reactive costs debuggability; virtual threads give the throughput we'd want from it |
| Spring Data JPA + Hibernate 6 | — | rich relational model; JPQL/`@EntityGraph` for the hot paths |
| PostgreSQL | 16 | JSONB, FTS, `SKIP LOCKED`, partial/expression indexes, CHECK constraints — it is our queue and our search engine too |
| Flyway | — | forward-only SQL migrations, reviewable in PRs (ADR-009) |
| Maven | — | multi-module, declarative, best-understood by coding agents |
| Bean Validation | Hibernate Validator | declarative request validation |
| springdoc-openapi | 2.x | OpenAPI 3.1 from annotations |
| MapStruct | — | compile-time entity↔DTO mapping; no runtime reflection surprises |
| JUnit 5 + Mockito + AssertJ | — | standard |
| Testcontainers | — | real PostgreSQL in tests; H2 would hide JSONB/FTS/`SKIP LOCKED` behaviour |
| ArchUnit | — | makes ADR-001's boundaries executable |
| Caffeine | — | in-process cache for the read-heavy content catalogue |
| Judge0 | — | sandboxed execution (ADR-006) |
| Docker + Compose | — | dev parity and deployment |

## Explicit exclusions
- **Lombok** — bytecode manipulation, IDE/agent friction. Java records cover DTOs;
  constructors are written out. Entities stay explicit.
- **Redis** — nothing needs distributed state in v1. Caffeine caches, Postgres queues,
  rate limiting is per-instance. Add it when we run >1 API instance.
- **RabbitMQ/Kafka** — see ADR-007.
- **Elasticsearch** — see ADR-010.
- **Kubernetes** — Compose on one VM serves 200 students comfortably (ADR: deployment doc).
- **GraphQL** — REST + OpenAPI gives the future frontend a firmer, cacheable contract.

## Deviations from the brief's baseline
Brief said "Spring Boot 3.x" (we pin 3.3.x) and listed Redis/RabbitMQ/MinIO as candidates —
we evaluated and excluded Redis and RabbitMQ (justified above), and made MinIO **optional**
behind the `FileStorage` port (ADR-008). Everything else matches.

## Consequences
Conventional, boring, heavily documented stack — exactly right for AI-assisted work.
Requires Java 21 and Docker for the full test suite.
