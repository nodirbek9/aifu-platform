# ADR-001 — Backend architecture: modular monolith + separate execution worker

**Status:** Accepted · **Date:** 2026-09 · **Supersedes:** —

## Context
~200 students, ~15 teachers, 15+ subjects, one small team, heavy AI-assisted development.
One hard constraint dominates: **untrusted student code must never run in the API process.**

## Options
1. **Layered monolith** (`controller/service/repository/entity` packages).
   Fast to start; at 15 domain areas the four god-packages become a coupling swamp, and an AI
   agent editing "the service package" has no boundary to respect. Rejected.
2. **Modular monolith** — one deployable, module-per-domain packages, enforced boundaries.
3. **Microservices** (auth, courses, assessment, coding, progress…). For 200 students this
   buys distributed transactions, 8 pipelines, network failure modes and cross-service
   debugging in exchange for independent scaling nobody needs. Rejected as
   complexity-for-status.
4. **Modular monolith + separate execution service.** = option 2 plus the one split that
   security *forces*.

## Decision
**Option 4.** A single Spring Boot API (`backend/api`) with enforced internal module
boundaries, plus a second deployable (`backend/execution-worker`) that is the only component
touching untrusted code.

Two processes, not eight, and the split follows a security boundary rather than an org chart.

Boundaries are enforced mechanically, not by convention:
- `ArchitectureBoundaryTest` (ArchUnit): no module may import another module's
  `persistence` package; layers depend inward only; no cycles between modules.
- `NoProcessExecutionInApiTest`: `Runtime.exec`, `ProcessBuilder`, `ScriptEngine` are banned
  from `backend/api` bytecode.
- The worker connects with a **restricted DB role** that can only read `submissions` /
  `coding_task_test_cases` and write `execution_*`. It cannot read `users` or
  `password_hash`. A compromised worker cannot dump the platform.

## Consequences
**Good:** one migration history, one transaction manager, local refactors stay local, trivial
deployment (2 containers + Postgres), module packages give AI agents an obvious blast radius,
and any module can be extracted later because it already talks through application services.

**Bad:** module boundaries are only as strong as the ArchUnit test — never delete it. The API
scales as one unit (acceptable: vertical scaling covers 50x this load). A bad migration
affects everything.

## Revisit when
Sustained >50 code submissions/minute (scale workers first, they are already separate); or a
module needs an independent release cadence or a different language; or a second institution
arrives and wants data isolation.
