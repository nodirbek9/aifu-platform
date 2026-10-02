# CLAUDE.md — AIFU Learning Platform

Read this file first. It is the contract for working in this repository.
Everything else is discoverable from here. Do not duplicate docs into this file.

## 1. What this is

The **AIFU Learning Platform** — a multi-subject university LMS for Aniq va Ijtimoiy Fanlar
Universiteti (AIFU). This repository holds the backend (`backend/`, the rules below) and, from v3,
the new frontend in `frontend/` (its own rules: `frontend/AGENTS.md`).

**What the product must do is in `docs/01-PRODUCT-SPEC.md`; how it is built and what is still to
build is in `docs/02-ARCHITECTURE.md`. Read both before adding a feature.**

Initial scale: ~200 students, ~15 teachers, 15+ subjects, 3 content languages (uz/ru/en).
Design target: grow 10-50x without a rewrite. Not designed for millions of users.

**Status: foundation stage.** The generic platform exists. Subject-specific academic
content is deliberately absent for all subjects except the two with supplied syllabi.
See `docs/academic-analysis/PENDING-INPUTS.md` before you invent any academic rule.

## 2. Architecture in one screen

**Modular monolith + one physically separate execution worker.** See `docs/decisions/ADR-001`.

```
backend/
├── api/                 Spring Boot app: HTTP API, all domain modules, Flyway owner
└── execution-worker/    Separate Spring Boot process. Claims jobs, drives the sandbox.
                         Runs untrusted student code. Restricted DB role. No API routes.
```

Inside `api`, one Java package per domain module under `uz.aifu.lms.<module>`:

```
common   iam        academics   curriculum  enrollment
progress assessment coding      execution   grading
files    notification analytics audit       search
scheduling  (student groups, rooms, bell schedule, timetable, teaching load — V019)
```

Each module has the same four layers. Depend inward only:

```
api/         REST controllers + DTOs        (may depend on app)
app/         use cases, @Transactional       (may depend on domain, port)
domain/      entities, value objects, rules  (depends on nothing)
persistence/ repositories, JPA queries       (implements app ports)
```

**Module boundary rule:** a module may import `common`, and may import another module's
`api.dto` + `app` service interfaces + `domain` read-only types. It must **never** import
another module's `persistence` package or another module's JPA entity for writing.
Enforced by `ArchitectureBoundaryTest` (ArchUnit). If you need cross-module writes, call
the owning module's application service.

## 3. Stack

Java 21 · Spring Boot 3.3.x · Spring Web · Spring Security · Spring Data JPA · PostgreSQL 16
· Flyway · Maven · Bean Validation · springdoc-openapi · MapStruct · Testcontainers · JUnit 5
· ArchUnit · Judge0 (sandbox) · Caffeine (in-process cache).

**Deliberately NOT used in v1:** RabbitMQ/Kafka (Postgres `SKIP LOCKED` queue is enough at
this scale), Redis (Caffeine + DB covers it), Elasticsearch (Postgres FTS), Kubernetes,
Lombok (records + explicit constructors instead). Each exclusion is argued in `docs/decisions/`.
**Do not add infrastructure without adding an ADR that kills one of these arguments.**

## 4. Commands

```bash
# Full local stack (API + Postgres + Judge0 + worker)
cp .env.example .env            # then edit secrets
docker compose up --build

# API only, DB in docker
docker compose up -d postgres
cd backend && ./mvnw -pl api spring-boot:run -Dspring-boot.run.profiles=dev

cd backend
./mvnw clean verify              # compile + unit + integration + ArchUnit
./mvnw -pl api test              # unit tests only (fast, no Docker)
./mvnw -pl api verify -Pit       # integration tests (needs Docker for Testcontainers)
./mvnw -pl api test -Dtest=QuizGradingServiceTest
./mvnw spotless:apply            # formatting (google-java-format AOSP)

# OpenAPI
open http://localhost:8080/swagger-ui.html
curl -s localhost:8080/v3/api-docs > docs/api/openapi.generated.json
```

Health: `GET /actuator/health/liveness`, `/actuator/health/readiness`.

## 5. Database rules (read before touching schema)

- **Flyway only.** `backend/api/src/main/resources/db/migration/V<n>__<snake_case>.sql`.
- **Never edit an applied migration.** Add a new one. `flyway.clean` is disabled in all
  profiles; `ddl-auto` is `validate` in every profile including tests.
- Dev/demo seed data lives in `db/seed/` and is loaded **only** under the `dev` profile
  via an extra Flyway location. Never put seed data in `db/migration/`.
- UUID primary keys, `gen_random_uuid()` default. `created_at`/`updated_at` on every table
  (`timestamptz`). `version` (optimistic lock) on every entity that can be concurrently edited.
- Every FK gets an index. Every `status` enum is a **Postgres `text` column + CHECK constraint**,
  not a native Postgres enum (cheap to extend) and a Java enum with `@Enumerated(STRING)`.
- Translations live in `<entity>_translations` tables keyed `(<entity>_id, locale)`. See ADR-005.
- **Historical data is append-only**: `grade_entries`, `enrollment_events`, `quiz_attempts`,
  `submissions`, `audit_log`. Supersede with a new row (`supersedes_id`, `revision`).
  Never `UPDATE` a published grade's score. Never hard-delete an attempt or submission.
- Soft delete exists on `subjects`, `courses`, `lessons`, `content_blocks` only (`deleted_at`).
  Everywhere else, delete is real or forbidden.

## 6. JPA rules

- All associations are `LAZY`. There is no `CascadeType.ALL` in this codebase; cascade is
  declared per-case and only for true composition (e.g. lesson → content blocks).
- No bidirectional `toString`/`equals`/`hashCode` across associations. Entities use
  **id-based** `equals`/`hashCode` (see `common.domain.BaseEntity`).
- Entities are never serialized to JSON. Controllers return records from `api/dto`. See §9.
- Fix N+1 with `@EntityGraph` or an explicit `join fetch` query, never with eager fetching
  or `hibernate.enable_lazy_load_no_trans`.
- `@Transactional` belongs on **application services only** — never on controllers, never
  on repositories. Read paths use `@Transactional(readOnly = true)`.

## 7. Security rules — non-negotiable

1. **Never execute student code in the API process.** No `Runtime.exec`, no `ProcessBuilder`,
   no scripting engine, no reflection-based eval anywhere in `backend/api`. A build-time
   check (`NoProcessExecutionInApiTest`) fails the build if these appear. The only
   legitimate path is: persist `submission` → enqueue `execution_job` → worker → sandbox.
2. Authorization is **server-side and centralized**. Use `@PreAuthorize` for coarse role/permission
   checks plus an `*AccessGuard` (see `iam.app.access`) for ownership. Never scatter `if (role == ...)`
   through controllers or repositories.
3. **Every student-scoped query filters by the caller's enrollment/user id in SQL** — not in Java
   after loading. `StudentDataIsolationIT` exists to prove it. Adding an endpoint that returns
   student data without an ownership filter is a release blocker.
4. Never return hidden coding test-case expectations, `solution_code`, password hashes,
   raw refresh tokens, or another student's identifiers in any DTO.
5. Passwords: BCrypt cost 12 via `PasswordEncoder` bean. Never log a credential, token,
   or full JWT. `SensitiveDataFilter` scrubs known keys from logs.
6. Teacher content is treated as **untrusted HTML**. Markdown in, sanitized on write with an
   allowlist (`common.content.ContentSanitizer`), stored sanitized. See `docs/security/content-security.md`.
7. Secrets come from env vars only. No secret is ever committed. `.env` is git-ignored.

## 8. Domain invariants (enforce at ALL of: DTO validation, service, DB constraint)

| Invariant | Where |
|---|---|
| A student has at most one non-terminal enrollment per course | partial unique index + service |
| A quiz attempt belongs to exactly one enrollment, and the caller must own it | FK + `AttemptAccessGuard` |
| Attempt number is contiguous per (quiz, enrollment) and ≤ `max_attempts` | unique index + service |
| A submission belongs to one (coding_task, enrollment) owned by caller | FK + `SubmissionAccessGuard` |
| Progress rows always hang off `enrollment_id`, never bare `user_id` | FK, no nullable user path |
| A teacher may mutate course content only if assigned to that course | `CourseAccessGuard` |
| Published grades are never overwritten, only superseded | append-only + DB trigger `trg_grade_entry_immutable` |
| A course cannot be published with an unpublished required lesson/quiz/task | `CoursePublicationValidator` |
| Assessment component weights of a course sum to the scheme total (100) | `CHECK` + validator |
| Locale set on required translations is complete before publish | `TranslationCompletenessValidator` |

## 9. API rules

- Base path `/api/v1`. Additive changes only within v1; breaking change ⇒ `/api/v2`.
- Request/response DTOs are **Java records** in `<module>/api/dto`. Never expose a JPA entity.
- Every list endpoint is paginated (`?page=&size=&sort=`), capped at `size=100`, and returns
  the `PageResponse<T>` envelope. No unbounded collection endpoints.
- Errors always use `ApiError` (`code`, `message`, `details`, `requestId`, `timestamp`,
  `path`) produced by `GlobalExceptionHandler`. Error `code` values are a closed enum
  (`common.error.ErrorCode`) that the frontend may switch on. **Never leak stack traces,
  SQL, or class names.** See `docs/api/error-model.md`.
- Every endpoint carries springdoc annotations with at least one example payload. An
  endpoint without OpenAPI documentation is incomplete.
- `Idempotency-Key` header is honoured on `POST /submissions` and `POST /quiz-attempts/*/submit`.

## 10. Educational domain in 10 lines

`Subject` (`fan`, e.g. `DAS1110`) is the catalogue entity: stable, owned by a `Department`,
attached to `AcademicProgramme`s, carries `LearningOutcome`s (`TN1..TNn`) and prerequisites.
A `Course` is **one delivery of a subject in one `Term`** — it holds credits, contact-hour
allocations per session type (`LECTURE`/`SEMINAR`/`PRACTICAL`/`LABORATORY`/`INDEPENDENT_STUDY`),
teachers, and the grading scheme. Students reach content only through an `Enrollment`.
`Course → Module → Lesson → ContentBlock` is the content tree; `ContentBlock` is typed and
localized. Anything a student *does* is an `Activity` (`QUIZ`, `CODING_TASK`, `MANUAL_TASK`,
`CASE_STUDY`, `PRESENTATION`, `GROUP_PROJECT`, `FILE_SUBMISSION`) bound to an
`AssessmentComponent` for grading. **New subject shapes = new `activity_type` + handler, never
a schema redesign.** Full model: `docs/domain/domain-model.md`.

## 11. Things you must not casually change

- The `execution` module's isolation boundary and anything in `docker/execution-worker/`
  or `docker/judge0/` (CPU/memory/pids/network limits). Security review required.
- `ErrorCode` values and `ApiError` shape — the frontend contract depends on them.
- Applied Flyway migrations.
- `activity_type` / `content_block_type` handling: extend the enum + add a handler.
  Do not special-case a subject name anywhere in code. Grep for `SubjectCode` — there are
  no subject-specific branches, and it must stay that way.
- The append-only grading model.
- Anything in `academic-materials/`.

## 12. Common pitfalls in this repo

- Forgetting to add the `_translations` row for a new localized entity ⇒ publish validator fails.
- Creating an `Activity` without `assessment_component_id` when `is_graded = true` ⇒ CHECK violation.
- Calling `progressService.recompute(...)` inside the same transaction as a long HTTP call.
- Adding a new module and forgetting `ArchitectureBoundaryTest`'s module registry.
- Writing an integration test without `@Tag("it")` ⇒ it runs in the fast unit profile and fails.
- Assuming locale `uz` has a Postgres FTS dictionary. It does not; `simple` + `unaccent` is used.
- The teaching languages are **Python, Java and C++** (V018, V020; product spec S-05). Go is
  still an enabled row from the first brief, but no task carries a Go solution.
- Timetable writes go through `TimetableService`, which locks the timetable row before the
  conflict check. Never insert a `timetable_entries` row any other way.

## 13. Where to look

| Need | File |
|---|---|
| Why the architecture is this way | `docs/decisions/` (ADR-001..010) |
| What the syllabi actually said | `docs/academic-analysis/syllabus-analysis.md` |
| What is still unknown | `docs/academic-analysis/PENDING-INPUTS.md` |
| Adding a new subject/syllabus | `docs/academic-analysis/HOW-TO-ONBOARD-A-SYLLABUS.md` |
| Entity-by-entity reference | `docs/domain/domain-model.md`, `docs/database/schema.md` |
| Endpoint contract | `docs/api/README.md`, `docs/api/openapi.yaml` |
| Sandbox threat model | `docs/code-execution/security-model.md` |
| What to build next | `docs/planning/phase-plan.md` |
| Task-specific coding rules | `skills/` (load the one matching your task) |

## 14. Working agreement for AI agents

1. Load the matching `skills/<topic>/SKILL.md` before editing that area.
2. Change one module per commit. Cross-module changes go through application services.
3. Migration + entity + DTO + validator + test land together, in that order.
4. Run `./mvnw -pl api test` before claiming anything works. **Never report a build, test
   run, or container start as passing unless you actually executed it and saw it pass.**
5. If a requirement is missing, add it to `PENDING-INPUTS.md` and stop. Do not invent
   academic requirements — especially not subject content for a syllabus we do not have.
