# AIFU Learning Platform — Backend Foundation: Final Report

## 1. Requirements interpretation

Two AIFU documents were supplied and both were read end to end:

| Document | Subject | Code | Shape |
|---|---|---|---|
| `IAKT_iqtisod_1k_2025_sillabus.docx` | Iqtisodiyotda AKT va tizimlar | `IAKTT1105` | 1 semester, 5 credits, 150 h = 30 lecture + 30 seminar + 90 independent, exam |
| `1.09. Dasturlash 142_bo'yicha_DI.doc` | Dasturlash | `DAS1110` | 2 semesters, 6 + 5 credits, 330 h total, curriculum ordinal 1.09 |

Five findings drove the design:

- **F1 — Independent study dominates.** 60% of IAKTT1105 hours and 64% of DAS1110 hours are
  *mustaqil ta'lim*. It is modelled as a first-class `session_type`, not an afterthought.
- **F2 — Session type is a lesson property.** IAKTT1105 lists Big Data as seminar S7 *and*
  independent topic MT9: the same topic in two delivery modes. One `lessons` table discriminated
  by `session_type`; topics are deliberately not deduplicated by title.
- **F3 — Hours are the institutional currency.** Totals must reconcile, so `lessons.allocated_hours`
  rolls up against `course_session_allocations` via `HourBudgetValidator` (warn on draft, block on publish).
- **F4 — Assessment is weighted, nested and gated.** `YaB = JN(30) + ON(30) + YaN(40)` with JN split
  10/10/10 and **60% minimum gates** that block the final exam. `assessment_components` is therefore a
  tree with `max_points`, `min_pass_percent` and `blocks_final`.
- **F5 — Independent work is variant-based.** DAS1110 requires *variativ* tasks with 1/3 cooperative,
  hence `activities.variant_strategy` + `activity_variants`. Group submission is deferred, not faked.

**Subject ≠ Course** is the load-bearing split, proved by DAS1110: one subject, two semesters,
different credits and different topic lists. No subject-specific rule was invented for the 13+
missing syllabi; every open question is registered in `docs/academic-analysis/PENDING-INPUTS.md`.

## 2. Architecture

**Modular monolith + one physically separate execution worker.** Microservices were rejected:
200 students and a small team do not pay for distributed transactions and 15 deploy pipelines.
The one split that *is* justified is security, not scale — untrusted student code cannot run in
the API process. Boundaries are enforced mechanically (ArchUnit) rather than by convention.
`docs/decisions/ADR-001-backend-architecture.md`.

## 3. Technology stack

Java 21, Spring Boot 3.3.x, Web MVC, Spring Data JPA, Spring Security, PostgreSQL 16, Flyway,
Maven, springdoc, MapStruct, JUnit 5 + Testcontainers + ArchUnit, Caffeine, Judge0, Docker.
Rejected with reasons: Lombok, Redis, RabbitMQ/Kafka, Elasticsearch, Kubernetes, GraphQL.
The job queue is `FOR UPDATE SKIP LOCKED` in PostgreSQL, which removes a broker *and* the
dual-write bug that comes with one. `docs/decisions/ADR-002`, `ADR-007`.

## 4. Modules

`common iam academics curriculum enrollment progress assessment coding execution grading files
notification analytics audit search`, each layered `api → app → domain`, `persistence → domain`.
Cross-module authorization uses ports declared in `iam.app.access` and implemented by the owning
module, so dependencies point inward and there are no cycles.

## 5. Database

15 migrations, ~60 tables, 6 analytics views. `Enrollment` is the spine: every student-owned row
(`*_progress`, `quiz_attempts`, `submissions`, `grade_entries`) references `enrollment_id`, never a
bare `user_id`, which makes cross-student access structurally unrepresentable rather than merely
forbidden. `grade_entries` and `audit_log` are append-only via triggers; grade corrections insert a
new revision with `supersedes_id`. Optimistic locking on every mutable aggregate. Partial unique
index `uq_enrollment_active` prevents duplicate enrollment at the database level.

**The multi-subject extension point:** `activities.activity_type` (8 values) plus
`content_blocks.block_type` (15 values). Adding Cybersecurity or Computer Networks means new rows
and possibly a new enum value plus handler — never a schema redesign. No code may branch on a
subject code; `grep -r 'DAS1110\|IAKTT1105' backend/*/src/main` must stay empty.

## 6. Authentication and authorization

BCrypt cost 12. JWT access token (15 min) + opaque single-use refresh token (30 days, stored as
SHA-256, rotated, grouped in families; **replay revokes the whole family and audits it**). One
generic `AUTH_INVALID_CREDENTIALS` for every login failure, with a dummy hash comparison to
equalize timing. Status re-checked on login *and* refresh.

Authorization is permission-based (40 permissions, 3 roles, mapped in the database so future roles
need no Java change), decided in `AccessGuard` — the single home of the ADMIN bypass. Reading an
object you have no relationship to returns **404, not 403**. `ADR-003`, `ADR-004`.

## 7. Multilingual architecture

Per-entity `*_translations` tables for structured, queryable fields; JSONB for content-block
payloads. Resolution order: `?locale=` → `Accept-Language` → user preference → course instruction
language → `uz`. Responses carry `resolvedLocale`, `availableLocales` and a `fallback` flag.
`courses.required_locales` defaults to `{uz}` so publishing is never blocked on invented ru/en
filler. `ADR-005`.

## 8. Coding execution

`SandboxEngine` port with Judge0 (default), `docker-run` (fallback) and `noop` (tests) adapters.
Submission and job are inserted in one transaction, the API returns `202` and the client polls.
Worker limits: CPU 5 s, wall 10 s, memory 256 MB, 64 pids, 64 KB output, 128 KB source,
`--network=none`, read-only rootfs, `tmpfs /tmp noexec`, non-root, all caps dropped, never
privileged, never a Docker socket. The worker holds a restricted database role with no access to
`users`, `refresh_tokens`, `audit_log` or `grade_entries`. Hidden test expectations and reference
solutions never reach a student DTO. `ADR-006`.

## 9. API

`/api/v1`, DTO records only (no entity ever leaves a controller), cursor-safe pagination with a
hard size cap, one error envelope (`code` / `message` / `details` / `requestId`), springdoc OpenAPI.
Endpoint areas are modelled on frontend use cases, not on entity count.

## 10. Testing — what was actually tested

**Nothing was executed.** The environment had Java 17, no Maven, no Docker and no network. No
compilation, no unit test, no integration test, no migration run, no container start, no HTTP call.
The §63 end-to-end checklist was **not** performed and no step in it may be reported as passing.
Two things were mechanically verified: all three `pom.xml` files parse as well-formed XML, and the
dev BCrypt hash in `V900` was generated and self-verified against `Dev!Pass123`.

## 11. Docker

Designed, not written: `docker/api/Dockerfile`, `docker/execution-worker/Dockerfile`, root
`docker-compose.yml` with `postgres`, `api`, `execution-worker`, `judge0` on an internal network,
Judge0 with `internet: false`, and service names (never `localhost`) as inter-container hosts.

## 12. Known limitations

1. **No build, test or container verification.** See `docs/BUILD-STATUS.md`. Expect compile errors.
2. Roughly half the Java is unwritten: `academics` onward, the worker loop, the test suite, the
   Dockerfiles, and seven `docs/` subdirectories. The 13-phase plan is the resumption order.
3. **DAS1110 is a C++ course** while the brief requires Python and Go. `programming_languages`
   carries `cpp` seeded *disabled* with a DO-NOT-DELETE note. Needs an AIFU decision.
4. Only 2 of 15+ syllabi were available. Academic dates, cohort sizes, grade-scale confirmation and
   13 subjects are open; all are registered in `PENDING-INPUTS.md`.
5. **PostgreSQL has no Uzbek dictionary.** `fts_config()` maps `uz` → `simple`: no stemming.
   Mitigated by trigram similarity plus a curated `search_synonyms` table. `ADR-010`.
6. Group submissions, proctoring, plagiarism detection, SSO and mobile push are deliberately deferred.

## 13. Future frontend

The API is prepared for login, student/teacher/admin dashboards, subject catalogue, course page,
module list, lesson page with a block-based content renderer, quiz interface, code editor with
starter code and visible examples, async submission polling, evaluation results, multi-level
progress, and the six analytics questions the brief lists (each backed by a database view).

## 14. ZIP

`aifu-learning-platform-backend.zip` — path reported in the reply. Excludes `.git`, `.idea`,
`target`, `build`, `node_modules`, `venv`, `.env`, keys, logs and temporary files. The original
AIFU syllabi are included unmodified under `academic-materials/syllabi/`.
