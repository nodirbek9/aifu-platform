# Project skills

Task-scoped rules for AI coding agents. **Load only the skill matching your task** — that is
the token-saving contract. `CLAUDE.md` is always in context; these are not.

| Skill | Load when |
|---|---|
| `architecture/` | adding a module, moving code, questioning a boundary |
| `java-backend/` | writing any Java in this repo |
| `spring-boot/` | config, beans, profiles, transactions, scheduling, caching |
| `database/` | any migration or schema change |
| `jpa-persistence/` | entities, repositories, queries, fetch strategy |
| `api-design/` | adding or changing an endpoint or DTO |
| `authentication/` | tokens, login, passwords, sessions |
| `authorization/` | permissions, guards, ownership, data isolation |
| `educational-domain/` | subjects, courses, lessons, activities, progress, grading |
| `coding-execution-security/` | anything touching submissions, the worker, or the sandbox |
| `testing/` | writing or fixing tests |
| `documentation/` | docs, ADRs, OpenAPI, this file |

Rules for editing skills: keep each under ~120 lines, state project-specific facts (not
general Java advice), always give the concrete file path, and delete anything that duplicates
`CLAUDE.md`.
