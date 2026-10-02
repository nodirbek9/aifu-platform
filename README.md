# AIFU Learning Platform

The learning platform of **Aniq va Ijtimoiy Fanlar Universiteti (AIFU)**: students read lessons,
solve programming tasks in Python, Java and C++ that are checked automatically, take quizzes and
see their grades; teachers see their weekly timetable, teach, keep attendance, author content and
grade; the administrator builds the timetable and manages groups, users and the academic
structure. Interface and content in Uzbek, Russian and English.

| Part | State |
|---|---|
| Backend (`backend/`) | Built and tested: 250 API operations, 320 automated tests |
| Frontend (`frontend/`) | Not started. Built next, phase by phase, from `docs/05-FRONTEND-PROMPTS.md` |
| Lesson content | Short lessons in Uzbek for five subjects; 53 coding tasks with solutions in three languages. Full three-language content is the next content phase |

## Where to start reading

| If you want to know | Read |
|---|---|
| What the product does, screen by screen | `docs/01-PRODUCT-SPEC.md` |
| How it is built, and what is built | `docs/02-ARCHITECTURE.md` |
| How to use the API from a client | `docs/03-API-GUIDE.md`, `docs/api/ENDPOINTS.md`, `docs/api/openapi.json` |
| How it must look | `docs/04-DESIGN-BRIEF.md` |
| How the frontend is built, phase by phase | `docs/05-FRONTEND-PROMPTS.md` |
| Rules for a coding agent | `AGENTS.md` (frontend), `CLAUDE.md` (backend) |
| Why the backend is designed this way | `docs/decisions/` (ADR-001…010) |

## Run it

Needs Docker Desktop.

```bash
cp .env.example .env        # set JWT_SECRET (openssl rand -base64 48) and the passwords
docker compose up -d --build
```

- API: http://localhost:8080 · Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health
- PostgreSQL is published on host port 5433.
- The stack runs the `dev` profile, which loads a demo semester. Every demo account has the
  password `Dev!Pass123`:

| Account | Role |
|---|---|
| `admin` | Administrator |
| `n.ayitbayev` | Teacher + administrator |
| `b.akbarov`, `a.zoxidov` | Teachers |
| `student3` (BK-DI26/1), `student1` (B-IQTR26/1), `xolmatov.u` (BK-DI25/1) | Students |

What to look at with each account: `docs/03-API-GUIDE.md` §9. The demo data and its password are
for development only: a production deployment runs without the `dev` profile and has no seed
accounts.

`docker compose down` stops the stack; `docker compose down -v` also deletes the database, and the
next start seeds it again.

## Layout

```
AGENTS.md  CLAUDE.md        Rules for coding agents (frontend / backend)
docs/                       Product spec, architecture, API guide and contract, design brief, ADRs
backend/api/                Spring Boot application: HTTP API, all domain modules, Flyway
backend/execution-worker/   Separate process that runs student code in a sandbox
docker/  docker-compose.yml Container images and the local stack
tools/content-seed/         Generates the demo content seed; verifies every task solution
tools/api-docs/             Generates docs/api/ENDPOINTS.md from the contract
academic-materials/         AIFU's original syllabi and branding. Read-only
.qoder/skills/              Skills for the frontend agent
```

## Backend in short

Java 21 · Spring Boot 3.3 · PostgreSQL 16 · Flyway · modular monolith (one package per module
under `uz.aifu.lms`, boundaries enforced by ArchUnit) plus one separate execution worker.

- **Student code never runs in the API process.** The API stores a job; the worker claims it
  (`FOR UPDATE SKIP LOCKED`) with a restricted database role and runs it in a container with no
  network, a read-only file system, CPU / memory / process / output limits and a non-root user.
- Authorization is permission-based and decided on the server. Reading something that is not
  yours answers 404, not 403.
- Grades and the audit log are append-only, enforced by database triggers.
- Teacher-authored HTML is sanitized on write.
- Access token 15 minutes; refresh tokens are single-use and rotated; reuse revokes the family.

```bash
cd backend
./mvnw spotless:apply            # formatting
./mvnw -pl api test              # unit tests, no Docker
./mvnw clean verify -Pit         # everything, with Testcontainers (about five minutes)
```

The contract `docs/api/openapi.json` is checked by a test; how to regenerate it after an API
change is in `CLAUDE.md` §4.

## Tests that prove the product, not just the code

- `DemoWalkthroughIT` walks the acceptance demo of the spec (§9) through the API on the seeded
  database.
- `DockerRunSandboxSmokeTest` (`-Dsandbox.smoke=true`) runs real Python, Java and C++ in the
  sandbox, including a compile error and an infinite loop.
- `node tools/content-seed/verify-solutions.mjs` compiles and runs every reference solution of
  every task in all three languages against its tests.
- `RunLoopIT` and `RestrictedRoleGrantsIT` run as the worker's restricted database role.

History of how the backend foundation was built and verified: `docs/BUILD-STATUS.md`,
`FINAL-REPORT.md` (both describe the state before v3 and are kept for the record).
