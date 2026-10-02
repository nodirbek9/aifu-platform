# PENDING INPUTS & DEFERRED DECISIONS

Single register of everything unknown or deliberately deferred. **An AI agent that hits one
of these must stop and ask, not invent.** Update this file instead of guessing.

Legend: 🔴 blocks content population · 🟡 blocks a feature · 🟢 nice to have

---

## 1. 🔴 Missing subject syllabi (13+)

Have: `IAKTT1105` (Economics ICT), `DAS1110` (Programming).
Missing, named in the brief: **Cybersecurity**, **Fundamentals of Artificial Intelligence**,
**Computer Networks**, plus 10+ unnamed.

**Consequence:** the platform ships with the generic engine and the two analyzed subjects
only. No lessons, quizzes, coding tasks, outcomes or hour budgets exist for any other
subject, and none may be created from the brief's illustrative bullet lists (§6 of the
brief) — those were examples of *shape*, not AIFU requirements.

**When they arrive:** follow `HOW-TO-ONBOARD-A-SYLLABUS.md`. Expected work per subject is
data entry + at most one new `activity_type` handler. If onboarding a syllabus seems to
require a schema change, that is a design bug — record it here first.

## 2. 🔴 Language conflict: syllabus says C++, product says Python & Go

`DAS1110` is unambiguously a C/C++ course: `Dinamik xotira`, `Ko'rsatkichlar`, `Shablonlar`,
`Konteynerlar`, `Visual Studio muhitida dasturlash`, and the reading list is Mo'minov
*Dasturlash 1/2*, Nazirov *C va C++ tili*, Horton *Beginning Visual C++ 2012*.

The build brief (§17) requires student code execution in **Python** and **Go**.

**Current resolution (reversible, non-destructive):** the execution layer is
language-agnostic. `programming_languages` is a data table; the sandbox selects an image per
language. Python and Go are seeded and enabled per the product requirement. **C++ is seeded
as `enabled = false`** with a note, so enabling it is a one-row change plus a sandbox image.

**Decision needed from AIFU:** is `DAS1110` being re-based on Python, or does the platform
need C++ auto-grading to serve the actual syllabus? This materially affects Phase 8.
Do not delete the C++ row to "clean up".

## 3. 🟡 Module structure inside a semester

Both documents give **flat, numbered topic lists** with no chapters, weeks, or units. AIFU's
`modules` grouping is therefore a platform construct. Seed data uses conservative thematic
grouping and every seeded module is labelled `grouping_source = 'PLATFORM_INFERRED'`.
Needs a teacher/o'quv-uslubiy bo'lim review before it is presented as official.

## 4. 🟡 Academic calendar

`academic_years` / `terms` need real start/end dates, exam periods and the o'quv jarayoni
jadvali (referenced repeatedly as the deadline authority: *"O'quv jarayonining jadvali
bo'yicha"*). Seeded dates are placeholders marked `is_placeholder = true`.

## 5. 🟡 Cooperative / group work (`DAS1110` requires 1/3 of independent work be group work)

Designed for, not implemented. `activities.collaboration_mode` (`INDIVIDUAL` | `GROUP`)
exists and is validated; `student_groups` / group submissions are **not** built. Deferred to
Phase 11. Do not fake it by sharing one student's submission.

## 6. 🟡 Variant-based independent tasks (`variativ tavsif`)

`activities.variant_strategy` + `activity_variants` exist with deterministic per-student
assignment (`hash(student_id, activity_id) % variant_count`). The *content* of variants
depends on the missing syllabi.

## 7. 🟡 Attendance (worth 10 of 100 points in `IAKTT1105`)

`assessment_components` reserves the `ATTENDANCE` component and `grade_entries` can carry
it, but there is no attendance-capture feature (no timetable, no check-in). Teachers can
only enter the component manually today. Needs a decision: manual entry, timetable
integration, or import from an existing AIFU system.

## 8. 🟡 Authoritative student & teacher rosters

No student numbers, group codes, or staff lists supplied. Is there an existing AIFU
HEMIS/university system to import from and sync with? Affects whether `users` is a master
or a mirror. Current assumption: **master**, admin-managed, with a documented import path.

## 9. 🟢 Translation sourcing (uz / ru / en)

All supplied material is Uzbek. The schema stores three locales and the publish validator
requires the course's `instruction_language` translation plus any locale in
`required_locales`. **Default `required_locales = {uz}`** so nothing fake is forced. Who
produces ru/en copy, and is machine translation acceptable as a draft? Unanswered.

## 10. 🟢 Branding

`academic-materials/branding/` is empty. No backend feature depends on the logo. When
supplied, it belongs to the future frontend plus (optionally) PDF certificate/transcript
generation, which is not in scope.

## 11. 🟢 Unused-but-declared session type

`LABORATORY` appears in the `IAKTT1105` form with **blank hours**, and `DAS1110` mentions
lab work in its methods section but allocates none. Kept in the enum because the
institutional form has it. Do not remove it as "dead".

---

## Deferred by engineering judgement (not blocked on AIFU)

| Deferred | Why | Phase |
|---|---|---|
| Group submissions | needs student groups | 11 |
| Elasticsearch | Postgres FTS is sufficient at 200 students (ADR-010) | reassess at ~50k lessons |
| Redis | Caffeine covers caching; single API instance | reassess on horizontal scale-out |
| RabbitMQ/Kafka | Postgres `SKIP LOCKED` queue is sufficient (ADR-007) | reassess at >50 submissions/min |
| Email/push notifications | in-app only in v1; `channel` column ready | 11 |
| Plagiarism / similarity detection | real need for a programming course, large scope | backlog |
| Proctoring, timed exam lockdown | `ON`/`YaN` are currently written, offline | backlog |
| Certificates / transcripts | needs branding + registrar rules | backlog |
| Multi-tenancy (several universities) | AIFU-only per brief §55; no tenant column added | backlog |
| SSO / HEMIS federation | depends on §8 | backlog |

## 12. 🟡 System configuration screen (Phase 10)

`phase-plan.md`'s Phase 10 scope names a "system configuration" admin surface, but no
schema, entity, or settings table exists anywhere in the codebase for runtime-configurable
values — every current knob (page size caps, rate limits, sandbox resource limits, JWT
expiry) lives in `application.yml`/env vars, changed by redeploy, not by an admin screen.
Before building one: what actually needs to be admin-editable at runtime versus
redeploy-time? No answer supplied yet. Do not invent a settings table without this.
