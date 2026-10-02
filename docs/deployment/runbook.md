# Operations runbook

Everything on this page was actually executed against the real `docker-compose.yml` stack during
Phase 12 hardening (see `../BUILD-STATUS.md`'s "Phase 12" section for the full findings) — not
copied from a template. Commands are exact; output shown is what was actually observed.

## Cold start (from nothing)

```bash
cp .env.example .env      # edit every CHANGE_ME value first
docker compose down -v    # only if you want a genuinely empty state
docker compose up --build -d
```

Readiness: `curl -s http://localhost:8080/actuator/health/readiness` → `{"status":"UP"}`.
Observed cold-start time from an empty volume to `UP`: ~15 seconds (image build time varies,
excluded).

**Known, self-healing startup race**: `execution-worker` starts its `@Scheduled` job-queue poller
before `api`'s Flyway migrations finish creating `execution_jobs` (the two containers start
concurrently; only `postgres` being healthy gates `api`/`execution-worker` in `docker-compose.yml`,
not "`api`'s migrations have finished"). You will see 1–2 `relation "execution_jobs" does not
exist` errors in `execution-worker`'s logs in the first ~2 seconds. This is not a defect: the
worker's poll loop retries on its own fixed delay and succeeds as soon as the table exists,
observed directly in a real cold-start run. No action needed. If this bothers an operator, the fix
is a `depends_on: condition: service_completed_successfully` on a migration-only init container —
not built here, since the self-healing behavior is already correct.

## Backup and restore

Rehearsed for real against the running `postgres` container (not simulated):

```bash
# Backup
docker compose exec postgres pg_dump -U aifu_app -d aifu_lms -Fc -f /tmp/aifu_backup.dump

# Restore into a fresh database (rehearsal — do this against a scratch DB first)
docker compose exec postgres psql -U aifu_app -d postgres -c "CREATE DATABASE aifu_restore_test;"
docker compose exec postgres pg_restore -U aifu_app -d aifu_restore_test /tmp/aifu_backup.dump
```

Verified: row counts in every restored table matched the source exactly (`SELECT count(*) FROM
users` — 8 in both, checked directly). `pg_restore` with `-Fc` produced no errors or warnings on a
clean target database.

For a real production restore (not a rehearsal): stop `api`/`execution-worker` first, drop and
recreate `aifu_lms`, restore, then start the app tier — restoring into a live database with active
connections is not something this rehearsal covered.

## Load / capacity observations

`ab -n 4000 -c 200` against `GET /api/v1/courses` (an authenticated, indexed, small-result-set
read) on a single `api` container, default config:

| Metric | Observed |
|---|---|
| Failed requests | 0 / 4000 |
| Median | 172 ms |
| p95 | 592 ms |
| p99 | 886 ms |
| Worst case | 2116 ms |

**Root cause of the tail, confirmed, not guessed**: `spring.datasource.hikari.maximum-pool-size`
defaults to 15 (`DB_POOL_MAX` env var). At 200 simultaneous requests each touching the database,
~185 requests queue waiting for one of 15 connections, which is exactly what produces the p95/p99
tail above — Tomcat accepts the connection immediately (its own thread pool defaults to 200), the
wait is entirely at the JDBC pool. CLAUDE.md's design target is **~200 total enrolled students**,
not 200 simultaneous in-flight requests — a real classroom traffic pattern is nowhere near this
adversarial. Still, if a real 200-concurrent-request spike is a concern, raise `DB_POOL_MAX` (and
Postgres's own `max_connections`, currently the image default) before relying on this number in
production; not changed here since the current default is a reasonable, conservative starting
point and changing it without a specific target deployment's Postgres sizing would be a guess.

## Hot-query sanity (`EXPLAIN ANALYZE`, dev-seeded data)

Run directly against the seeded dev database inside the `postgres` container:

```bash
docker compose exec postgres psql -U aifu_app -d aifu_lms -c "EXPLAIN ANALYZE <query>"
```

Three representative queries, all well under the 200 ms target on seeded data:

| Query | Plan | Execution time |
|---|---|---|
| Active-enrollment lookup (the `uq_enrollment_active` partial index) | Index Scan | 0.224 ms |
| Lesson list for a course, ordered (module + lesson join) | Hash Join, Bitmap Index Scan on `uq_module_position` | 0.315 ms |
| Audit log, newest first (`idx_audit_log_occurred`) | Index Scan + incremental sort | 0.059 ms |

At the seeded dataset's current size (dozens of rows per table, not thousands) these numbers say
little about production scale directly, but they confirm every one of these hot paths is actually
using its intended index, not falling back to a sequential scan — the thing that would matter once
row counts grow.

## Secret hygiene

Checked directly against the tracked git history and current tree, not assumed:

- `.env` is git-ignored (`.gitignore`: `.env`, `.env.*`, `!.env.example`); no `.env` file has ever
  been committed.
- No AWS-style access key pattern (`AKIA[0-9A-Z]{16}`) or PEM private-key block exists in any
  tracked file.

## Logging / aggregation

`logback-spring.xml` already ships structured JSON (Logstash encoder, `requestId`/`userId` from
MDC, a `service` field) under the `prod` Spring profile; `dev`/`test`/default use a plain
human-readable console pattern. This is already aggregator-ready (Loki/ELK/CloudWatch-style
sidecars all consume line-delimited JSON on stdout) — no code change needed to start shipping logs
once a specific aggregation target is chosen. No aggregator sidecar is configured in
`docker-compose.yml`; that is a deployment-target decision (which aggregator, which retention)
that cannot be made generically here.

## What this runbook deliberately does not cover

- **Reverse proxy + TLS**: no `nginx`/Caddy config exists in this repo. Certificates and a real
  public hostname are deployment-target specifics no generic runbook entry can safely invent —
  see `docs/planning/phase-plan.md`'s Phase 12 scope note.
- **Dependency CVE scanning**: no scanner (OWASP dependency-check, Snyk, etc.) is wired into the
  build. Adding one is straightforward but was not done here — running one requires either an NVD
  API key or a long first-run database download, and a scan without triage of its findings is not
  worth less than half-doing.
- **Full §64 execution-security matrix and the `docker-run` sandbox verification**: already run
  and documented in Phase 8 (`docs/code-execution/security-model.md`) — not re-run here since
  nothing in Phases 9–12 touched the sandbox itself.
