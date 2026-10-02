---
name: database
description: PostgreSQL + Flyway rules. Load before writing any migration or changing the schema.
---
# Database rules

Flyway, forward-only (ADR-009). `backend/api/src/main/resources/db/migration/V<n>__<name>.sql`.

## Never
- Edit an applied migration. Add a new one.
- Use `hibernate.ddl-auto` beyond `validate`, or enable `flyway.clean`.
- Put academic/demo content in `db/migration/` — that is `db/seed/` (dev profile only).
- `DELETE` from `grade_entries`, `quiz_attempts`, `submissions`, `enrollment_events`,
  `audit_log`. They are append-only.
- Add a native Postgres `ENUM`. Use `text` + `CHECK (col IN (...))`.

## Always
```sql
id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
created_at   timestamptz NOT NULL DEFAULT now(),
updated_at   timestamptz NOT NULL DEFAULT now(),   -- + trg_set_updated_at trigger
version      bigint NOT NULL DEFAULT 0             -- if concurrently editable
```
- Index **every** FK. Name indexes `idx_<table>_<cols>`, constraints `uq_/ck_/fk_<table>_<cols>`.
- `ON DELETE CASCADE` only for true composition (lesson→blocks, question→options,
  attempt→attempt_questions). Otherwise `RESTRICT`.
- Money/score columns: `numeric(6,2)`. Never `float`/`double precision`.
- Localized text goes in `<entity>_translations(<entity>_id, locale, ...)`,
  `UNIQUE(<entity>_id, locale)`, `ON DELETE CASCADE`, plus a generated `search_vector` using
  `fts_config(locale)`.
- Ordering columns are `position int NOT NULL` with `UNIQUE(parent_id, position) DEFERRABLE
  INITIALLY DEFERRED` so a reorder can swap inside one transaction.

## Patterns used here
- "At most one active X per Y": partial unique index, e.g.
  `CREATE UNIQUE INDEX uq_enrollment_active ON enrollments(course_id, student_user_id)
   WHERE status IN ('PENDING','ACTIVE','SUSPENDED');`
- Queue: `SELECT ... FOR UPDATE SKIP LOCKED` (see `V011`, ADR-007).
- Immutability: trigger `trg_grade_entry_immutable` (see `V012`).
- Soft delete: `deleted_at timestamptz` on `subjects`, `courses`, `lessons`, `content_blocks`
  **only**, always paired with a partial index `WHERE deleted_at IS NULL`.

## Checklist for a schema change
1. Migration written and readable. 2. Indexes for new FKs + query patterns.
3. CHECK constraints for every enum and every numeric range.
4. Entity updated to match exactly (Hibernate `validate` will catch drift at boot).
5. `./mvnw -pl api verify -Pit` — Testcontainers applies the migration on an empty DB.
6. Destructive? Two-phase it (add+backfill now, drop later) and note the rollback plan.
