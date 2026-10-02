# ADR-011 — Grading: append-only entries with supersession

**Status:** Accepted

## Context
Brief §20: "avoid destructive overwriting of historical grading data". `IAKTT1105` defines a
weighted, gated rubric (JN 30 = 10+10+10, ON 30, YaN 40, with 60% admission gates) and
`DAS1110` defines 100→5 bands. Grades carry real academic consequence (`akademik qarzdor`),
so they need an audit trail a registrar can defend.

## Decision
**`grade_entries` is append-only.**

- A grade is never `UPDATE`d. A correction inserts a new row with `revision = previous + 1`
  and `supersedes_id = previous.id`; the old row gets `status = 'REVISED'`.
- A trigger (`trg_grade_entry_immutable`) rejects any `UPDATE` that changes `score`,
  `max_score`, `assessment_component_id` or `enrollment_id` on a row whose status is
  `PUBLISHED`. Enforced in the database, so a buggy service or a stray SQL console cannot
  quietly rewrite history.
- Every entry records `source` (`QUIZ` | `CODING` | `MANUAL` | `ATTENDANCE` | `IMPORT`),
  `source_ref_type` + `source_ref_id` (the attempt or submission it came from),
  `evaluator_user_id`, `evaluated_at`, `published_at`.
- Auto-graded quiz/coding results produce `grade_entries` too, so the gradebook has one shape
  regardless of origin, and a teacher override is just a superseding `MANUAL` row that
  preserves what the machine said.
- Deletion is `status = 'VOIDED'` plus a reason — never a `DELETE`.
- The current gradebook reads a view (`v_current_grade_entries`) filtered to the latest
  non-voided revision per (enrollment, component, source_ref), so consumers never have to
  know about revisions.
- `enrollment.final_score` is **derived** from published entries by `FinalScoreCalculator`
  (weights from `assessment_components`) and is recomputed, never hand-edited. Admission and
  academic-debt flags are computed from the same source with the per-component
  `min_pass_percent` gates, and the calculation inputs are stored on the enrollment snapshot
  so a later rubric change cannot retroactively alter a past verdict.

## Consequences
Full defensible history, safe teacher corrections, machine-vs-human grade transparency.
Costs: reads go through a view, the table grows (trivially — a few rows per student per
course), and consumers must respect `status`. `GradeEntryImmutabilityIT` exists to prove the
trigger works.
