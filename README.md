# AIFU Learning Platform — Backend

Backend for a multi-subject university learning platform for **Aniq va Ijtimoiy Fanlar
Universiteti (AIFU)**. Initial target: ~200 students, multiple teachers, administrators,
15+ subjects, content in **Uzbek / Russian / English**, quizzes, programming exercises with
**automated, sandboxed Python and Go evaluation**, per-student progress and teacher analytics.

**Backend only.** No frontend is included by design. The REST API is the contract a future
frontend integrates against.

---

## 1. Architecture

**Modular monolith + one isolated execution worker.** Two deployables, not microservices:

| Deployable | Purpose | Network |
|---|---|---|
| `backend/api` | All business modules, single Spring Boot app, single database | Public (behind reverse proxy) |
| `backend/execution-worker` | Runs untrusted student code. No HTTP port, pulls jobs from Postgres | Internal only |

The split exists for exactly one reason: **student code must never execute inside the API
process.** See `docs/decisions/ADR-001-backend-architecture.md` and
`docs/decisions/ADR-006-code-execution.md`.

Modules (`uz.aifu.lms.<module>`), each with `api → app → domain` and `persistence → domain`:

```
common  iam  academics  curriculum  enrollment  progress  assessment
coding  execution  grading  files  notification  analytics  audit  search
```

Boundaries are meant to be enforced mechanically by `ArchitectureBoundaryTest` (ArchUnit) and
`NoProcessExecutionInApiTest`. See §9 for the honest status of the test suite.

## 2. Stack

Java 21 · Spring Boot 3.3.x · Spring Web MVC · Spring Data JPA / Hibernate 6 · Spring Security
· PostgreSQL 16 · Flyway · Maven · springdoc-openapi · MapStruct · Bean Validation ·
JUnit 5 + Mockito + AssertJ + Testcontainers + ArchUnit · Caffeine · Judge0 · Docker.

**Deliberately excluded:** Lombok, Redis, RabbitMQ/Kafka, Elasticsearch, Kubernetes, GraphQL.
Each exclusion is justified in `docs/decisions/ADR-002-technology-stack.md`. At 200 students a
broker and a search cluster are operational cost with no corresponding benefit.

## 3. Repository layout

```
CLAUDE.md                  Context for AI coding agents. Read this first.
README.md                  This file.
academic-materials/        ORIGINAL AIFU syllabi. Read-only. Never edit or regenerate.
  syllabi/                 Source .docx / .doc, unmodified, original filenames.
  branding/                (pending — logo to be added by AIFU)
  _extracted/              Derived plain text for grepping. Regenerable.
docs/                      Engineering documentation.
  academic-analysis/       What the syllabi actually say, and what is still unknown.
  architecture/ domain/ database/ api/ security/ code-execution/
  development/ testing/ deployment/ decisions/ planning/
skills/                    12 project-specific skills for Claude Code.
backend/api/               The Spring Boot application.
backend/execution-worker/  The sandboxed code runner.
docker/                    Dockerfiles and container config.
docker-compose.yml         Local stack.
.env.example               Every variable the architecture needs. No real secrets.
```

## 4. Local setup

```bash
cp .env.example .env            # then replace every CHANGE_ME value
docker compose up --build
```

Services: `postgres` (5432), `api` (8080), `execution-worker`, `judge0` (internal).
Inside Docker the API reaches the database at host **`postgres`**, never `localhost` —
`localhost` inside a container is the container itself.

Without Docker: PostgreSQL 16 on `localhost:5432`, then

```bash
cd backend && ./mvnw spring-boot:run -pl api -Dspring-boot.run.profiles=dev
```

## 5. Migrations

Flyway, forward-only. `spring.jpa.hibernate.ddl-auto=validate` in **every** profile, and
`flyway.clean-disabled=true`. Hibernate never creates or drops a table in this project.

- Schema: `backend/api/src/main/resources/db/migration/V0xx__*.sql` (V001–V015)
- Dev-only seed: `backend/api/src/main/resources/db/seed/V9xx__*.sql`, loaded under the `dev`
  profile only.

Never edit an applied migration. Destructive changes go in two phases (add + backfill, then
drop in a later release). Rules: `docs/decisions/ADR-009-database-migrations.md`.

## 6. Tests

```bash
cd backend
./mvnw test                     # unit + architecture tests
./mvnw verify -Pit               # adds integration tests (needs Docker for Testcontainers)
```

## 7. API documentation

- OpenAPI JSON: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui.html`
- Error model and conventions: `docs/api/`

All endpoints are under `/api/v1`. Every error uses one envelope:

```json
{ "code": "RESOURCE_NOT_FOUND", "message": "Course not found", "details": {}, "requestId": "..." }
```

`requestId` is echoed in the `X-Request-Id` response header and appears on every log line for
that request.

## 8. Security notes

- BCrypt cost 12. Plaintext passwords are never stored, logged, or audited.
- JWT access token (15 min) + opaque single-use refresh token (30 days). Refresh tokens are
  stored as SHA-256 hashes, rotated on use, and grouped into families: **re-presenting a
  consumed token revokes the entire family** and writes an audit record.
- Authorization is permission-based, decided server-side in `iam.app.access.AccessGuard`.
  The ADMIN bypass exists in that one class and nowhere else.
- Reading an object you have no relationship to returns **404, not 403** — a 403 confirms the
  object exists, which is an enumeration oracle in a 200-student cohort.
- Student-owned rows hang off `enrollment_id`, never a bare `user_id`, and student queries are
  scoped in SQL rather than loaded-then-checked.
- Teacher-authored content is sanitized on write by `ContentSanitizer` (OWASP allowlist).
  Raw HTML and `<script>` are rejected, not escaped. See `docs/security/content-security.md`.
- `grade_entries` and `audit_log` are append-only, enforced by database triggers.
- Secrets come from the environment. `.env` is git-ignored; `.env.example` holds placeholders.

## 9. Code execution architecture

```
Student → POST /api/v1/submissions → persist submission + job in ONE transaction → 202 Accepted
                                          ↓ (FOR UPDATE SKIP LOCKED)
                            execution-worker claims job with a lease
                                          ↓
                          Judge0 / docker-run sandbox, network disabled
                                          ↓
              per-test results → scoring → grade entry → progress update
```

Limits actually applied: CPU 5 s, wall 10 s, memory 256 MB, 64 processes, 64 KB output,
128 KB source, `--network=none`, read-only rootfs, `tmpfs /tmp` with `noexec`, non-root user,
all capabilities dropped. Never `--privileged`, never a mounted Docker socket. The worker uses a
restricted database role with no access to `users`, `refresh_tokens`, `audit_log` or
`grade_entries`. Hidden test expectations and reference solutions are never returned to a
student. Full model: `docs/code-execution/security-model.md`.

## 10. Development workflow

1. Read `CLAUDE.md`.
2. Read the relevant file in `skills/` before touching a module.
3. Schema change → new Flyway migration, never an edit to an applied one.
4. Check `docs/academic-analysis/PENDING-INPUTS.md` before inventing an academic rule.
5. Run the quality gate in `docs/planning/phase-plan.md` at the end of every phase.

## 11. Build status — read before you trust anything here

This repository was produced in an environment with **Java 17, no Maven, no Docker and no
network access**. Nothing has been compiled, no test has been run, and no container has been
started. `docs/BUILD-STATUS.md` states precisely what exists and what remains. The first real
quality gate is `./mvnw clean verify` on your machine.
