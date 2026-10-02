# ADR-009 — Database migrations: Flyway, forward-only, seed data separated

**Status:** Accepted

## Decision
- **Flyway** with plain SQL. Not Liquibase (XML/YAML abstraction hides Postgres features we
  deliberately use: partial indexes, generated `tsvector` columns, CHECK constraints,
  triggers). Not `hibernate.ddl-auto` — ever.
- `spring.jpa.hibernate.ddl-auto=validate` in **every** profile, tests included. Hibernate
  verifies the Flyway schema and refuses to start on drift. Drift becomes a startup failure
  instead of a production surprise.
- `flyway.clean-disabled=true` everywhere. Testcontainers gets a fresh database per run, so
  clean is never needed.
- **Forward-only.** Never edit an applied migration; add a new one. Rollback = a compensating
  migration plus a restore plan, both written before a destructive change ships.
- Naming: `V<seq>__<snake_case_description>.sql`, one logical concern per file, sequence
  never reused.
- **Seed data is not schema.** `db/migration/` holds structure + reference data that the code
  depends on (permissions, roles, grade scale, block types, languages). Dev/demo academic
  content lives in `db/seed/` and is only loaded when the `dev` profile adds it as an extra
  Flyway location. Production never runs it.
- Destructive steps (drop/rename/narrow a column) are **two-phase**: add-and-backfill in one
  release, remove in a later one, so a rollback of the app does not break the DB.
- Data-heavy backfills go in `R__`/separate migrations with explicit batching, never inside a
  schema migration that holds an `ACCESS EXCLUSIVE` lock.

## Consequences
Reviewable SQL diffs, deterministic environments, no accidental data loss, and Hibernate
mapping errors caught at boot. Costs discipline: schema changes are a deliberate artifact,
not a side effect of editing an entity.
