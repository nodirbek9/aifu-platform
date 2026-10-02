# db/seed — DEVELOPMENT FIXTURES ONLY

Loaded **only** under the `dev` profile, via an extra Flyway location
(`application-dev.yml`: `spring.flyway.locations=classpath:db/migration,classpath:db/seed`).
The `prod` profile lists `classpath:db/migration` only, so **production never executes these
files** (ADR-009).

## What is real vs illustrative

| File | Content | Provenance |
|---|---|---|
| `V900__dev_seed_institution.sql` | AIFU department, programmes, academic years, terms, users | Institution/programme codes are **verbatim from the syllabi**. Users are fake. |
| `V901__dev_seed_subjects.sql` | `IAKTT1105` and `DAS1110`: titles, aims, outcomes, hours, rubric, topic lists, literature, benchmark links | **Verbatim transcription** of the supplied documents. |
| `V902__dev_seed_fixtures.sql` | One demo quiz, one demo coding task, enrollments, progress | **Illustrative fixtures, NOT AIFU academic requirements.** Marked in-file. |

Only the two subjects with supplied syllabi exist. Cybersecurity, AI and Networks are
**deliberately absent** — see `docs/academic-analysis/PENDING-INPUTS.md` §1.

## Development credentials — fake, and only fake

Every seeded account uses the password **`Dev!Pass123`**.

| Login | Role |
|---|---|
| `admin@dev.aifu.local` | ADMIN |
| `teacher.akbarov@dev.aifu.local` | TEACHER (both subjects) |
| `teacher.zoxidov@dev.aifu.local` | TEACHER (Programming) |
| `student1@dev.aifu.local` … `student5@dev.aifu.local` | STUDENT |

These are obviously-fake `.local` addresses with an obviously-fake password. **No real AIFU
credential appears anywhere in this repository.** The stored value is a BCrypt cost-12 hash,
not plaintext. If this data ever reaches a non-dev environment, that is a deployment bug: the
`prod` profile cannot load it, and `DevDataGuard` fails startup if `dev` seed users are found
outside the `dev` profile.
