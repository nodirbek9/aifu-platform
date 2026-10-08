# ADR-016 — Question banks as the quiz's own questions, per-attempt draws, audited extra attempts

**Status:** Accepted (2026-10-07, phase 5)

## Context
Owner: a lesson test has a bank of 8–10 questions, 5 are drawn per attempt, pass mark 60 %, two
attempts, and the second attempt must draw *other* questions. The midterm has 30 questions from a
course bank, 40 minutes, 60 %, two attempts. An admin may grant one extra attempt when attempts are
used up, audited.

What exists (V009, `assessment.app.AttemptService`): questions belong to one quiz;
`quizzes.question_pool_size` already draws a random subset; the served questions are stored per
attempt in `quiz_attempt_questions` (so a reload never rerolls — `GET /attempts/{id}` reads the
snapshot). `max_attempts` is checked in the service only; seeds set it to 3. Uniqueness
`uq_quiz_attempt_number` and `uq_quiz_attempt_open` stop double starts.

## Decision
1. **Bank = the quiz's questions.** No new bank table: a TEST quiz holds 8–10 questions with
   `question_pool_size = 5`; a MIDTERM quiz holds the course bank (target ≥ 60) with
   `question_pool_size = 30`. Reason: the schema, editors, importer and graders already work.
2. **Draw rule** (`AttemptService.serveQuestions`): candidates = questions never served to this
   enrollment in earlier attempts of this quiz; draw randomly (`SecureRandom`) from them; if too
   few, fill with previously served ones, least-recently served first. The draw is written to
   `quiz_attempt_questions` in the start transaction — that row set *is* the attempt.
3. **Attempt allowance** = `quizzes.max_attempts` + number of rows in a new append-only
   `quiz_attempt_grants (quiz_id, enrollment_id, granted_by, reason, created_at)`.
   CLAUDE.md §8 invariant becomes "attempt number is contiguous and ≤ max_attempts + grants".
   Enforced in: DTO (reason required), service, and DB — a `BEFORE INSERT` trigger on
   `quiz_attempts` that takes `pg_advisory_xact_lock` on (quiz, enrollment) and checks
   `attempt_number = max + 1` and `≤ allowance`. Existing rows are never touched (append-only).
4. **Grant rule:** admin only (new permission `QUIZ_ATTEMPT_GRANT`), only when the student has not
   passed, has no open attempt and has used the whole allowance — so at most one unused grant can
   exist. Same advisory lock as the trigger, so a grant and a start cannot interleave.
   Every grant writes `audit_log` action `QUIZ_ATTEMPT_GRANTED` with quiz, enrollment and reason.
5. **Concurrency:** two tabs starting → the loser hits `uq_quiz_attempt_open` →
   `ATTEMPT_ALREADY_OPEN`; double submit → existing `Idempotency-Key` replay; time-out → existing
   lazy `closeIfTimeIsUp`.
6. **Settings by role** (set by the importer and by migration V033 for existing data):
   TEST: max 2, pool 5, pass 60, `reveal_policy = AFTER_LAST_ATTEMPT`, shuffle options.
   PRACTICE (checked-answer task): no limit, pass 100, `reveal_policy = NEVER`.
   MIDTERM: max 2, pool 30, 2400 s, pass 60, `AFTER_LAST_ATTEMPT`.

## Consequences
- A student who already used 3 attempts keeps them (history intact); with max 2 they simply have
  none left. ADR-018 makes sure that cannot lock them out of lessons they already reached.
- `AFTER_LAST_ATTEMPT` is computed against the allowance, so a grant hides answers again for the
  extra attempt; the extra attempt draws unseen questions first.
- The student quiz view gains additive fields `attemptsAllowed`, `attemptsUsed`.

## Revisit when
Teachers need to share questions across quizzes or tag them by learning outcome — then a
`question_banks` table with quizzes drawing from it replaces rule 1.
