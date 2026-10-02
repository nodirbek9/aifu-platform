# Architecture Decision Records

| ADR | Decision | Status |
|---|---|---|
| [001](ADR-001-backend-architecture.md) | Modular monolith + separate execution worker | Accepted |
| [002](ADR-002-technology-stack.md) | Java 21 / Spring Boot 3.3 / PostgreSQL 16 | Accepted |
| [003](ADR-003-authentication.md) | JWT access token + rotating opaque refresh token | Accepted |
| [004](ADR-004-authorization.md) | Role→permission authorities + centralized access guards | Accepted |
| [005](ADR-005-multilingual-content.md) | Per-entity translation tables (+ JSONB block payloads) | Accepted |
| [006](ADR-006-code-execution.md) | Judge0 behind a `SandboxEngine` port, network-isolated | Accepted |
| [007](ADR-007-background-jobs.md) | PostgreSQL `SKIP LOCKED` job queue, no broker | Accepted |
| [008](ADR-008-file-storage.md) | `FileStorage` port; local FS in dev, S3-compatible in prod | Accepted |
| [009](ADR-009-database-migrations.md) | Flyway, forward-only, seed data separated | Accepted |
| [010](ADR-010-search.md) | PostgreSQL full-text search, no search engine | Accepted |
| [011](ADR-011-grading-model.md) | Append-only grade entries with supersession | Accepted |

Template: Context → Options → Decision → Consequences → Revisit when.
