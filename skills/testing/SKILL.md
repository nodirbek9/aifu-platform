---
name: testing
description: Test strategy, tags, Testcontainers and the mandatory coverage list.
---
# Testing rules

`backend/api/src/test/java/...` mirrors the main package layout.

## Kinds and tags
| Kind | Name | Tag | Needs Docker |
|---|---|---|---|
| Unit | `*Test` | — | no |
| Integration (DB) | `*IT` | `@Tag("it")` | yes (Testcontainers) |
| Architecture | `ArchitectureBoundaryTest`, `NoProcessExecutionInApiTest` | — | no |
| Security/authorization | `AuthorizationMatrixIT`, `StudentDataIsolationIT` | `@Tag("it")` | yes |

`./mvnw -pl api test` runs untagged only (fast, CI-cheap, no Docker).
`./mvnw -pl api verify -Pit` adds the tagged ones. **Forgetting `@Tag("it")` on a DB test
breaks the fast profile** — the most common mistake in this repo.

## Rules
- Integration tests extend `AbstractIntegrationTest` (one shared, reused Postgres 16 container,
  `webEnvironment = RANDOM_PORT`). Never point a test at a real database.
- **No `@Transactional` on `AbstractIntegrationTest`.** An HTTP call through `TestRestTemplate`
  is served on its own thread with its own DB connection, so a test-method transaction is never
  visible to it — a fixture "created" in the test looks like it doesn't exist to the server (e.g.
  a freshly inserted user gets `AUTH_INVALID_CREDENTIALS` on login because the row was never
  committed). Give fixtures unique identifiers instead (`support.fixtures.UserFixtures`) so
  leftover rows in the shared container never collide across tests.
- **Never H2.** It hides JSONB, FTS, `SKIP LOCKED`, partial indexes and CHECK behaviour — i.e.
  most of our design.
- Build test data with `test/fixtures/` builders (`CourseFixture.aPublishedCourse()`), not
  hand-rolled entity graphs per test.
- Assert with AssertJ. One behaviour per test. Name it
  `methodName_condition_expectedOutcome`.
- Mockito only for genuine boundaries (sandbox engine, storage, clock). Do **not** mock
  repositories in an integration test.
- Time: inject `Clock`; use `Clock.fixed` in tests. No `Instant.now()` in production code.
- Sandbox: `NoopSandboxEngine` in tests. A test must never start a real container that runs
  student code.

## Mandatory coverage (brief §41) — keep this list green
user creation · login (success/failure/lockout) · token refresh + rotation + reuse detection ·
authorization per role × endpoint · course access · enrollment (duplicate, concurrent,
withdraw/re-enroll) · lesson retrieval (published only, enrolment-scoped) · lesson progress
(idempotent, roll-up) · quiz attempt lifecycle · quiz grading per question type · attempt
limits · coding submission creation (+ idempotency) · execution result handling · verdict
mapping · grading append-only + supersession · final score with the 60% gates · student data
isolation · teacher permissions (own courses only) · admin permissions · content sanitization
· translation fallback · publication validation.

## Execution matrix (brief §64) — Phase 12, needs real Docker
valid Python · valid Go · syntax error · compilation failure · runtime error · wall timeout ·
infinite loop · memory exhaustion · fork bomb · network attempt (must fail) · filesystem write
attempt (must fail) · oversized output · oversized source · invalid language · wrong output ·
correct output.

## Honesty rule
Never write "tests pass" in a commit message, a doc, or a report unless you ran them and saw
them pass. State what you ran and what you did not.
