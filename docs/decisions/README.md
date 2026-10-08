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
| [012](ADR-012-progress-iam-dependency.md) | progress consumes AccessGuard directly | Accepted |
| [013](ADR-013-split-curriculum-locator.md) | CurriculumLocator split into one port per owning module | Accepted |
| [014](ADR-014-progress-assessment-events.md) | Cross-module notification via Spring events | Accepted |
| [015](ADR-015-learning-path-gate.md) | One server-side learning-path gate; reading time measured by the server | Accepted |
| [016](ADR-016-question-banks-and-attempts.md) | Question banks, per-attempt draws, audited extra attempts | Accepted |
| [017](ADR-017-midterm-and-week-8.md) | Midterm bound to ON, opens in term week 8, gates later lessons | Accepted |
| [018](ADR-018-learning-path-rollout.md) | Learning path rollout without data loss or lock-outs | Accepted |

Template: Context → Options → Decision → Consequences → Revisit when.
