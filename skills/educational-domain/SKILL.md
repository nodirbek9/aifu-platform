---
name: educational-domain
description: AIFU academic concepts and platform invariants. Load before touching subjects, courses, lessons, activities, progress or grading.
---
# Educational domain

Grounding: `docs/academic-analysis/syllabus-analysis.md`. Full model:
`docs/domain/domain-model.md`. **Unknowns: `docs/academic-analysis/PENDING-INPUTS.md` — read
it before inventing anything.**

## AIFU vocabulary → code
| Uzbek | Meaning | Entity |
|---|---|---|
| fan | subject | `Subject` |
| fan kodi | subject code (`DAS1110`) | `subjects.code` |
| kafedra | department | `Department` |
| ta'lim yo'nalishi | degree programme (`60610600`) | `AcademicProgramme` |
| semestr | semester/term | `Term` |
| kredit | ECTS-style credit | `courses.credits` |
| ma'ruza | lecture | `SessionType.LECTURE` |
| seminar | seminar | `SessionType.SEMINAR` |
| amaliy mashg'ulot | practical | `SessionType.PRACTICAL` |
| laboratoriya | lab (declared, unused) | `SessionType.LABORATORY` |
| mustaqil ta'lim | independent study (60%+ of hours!) | `SessionType.INDEPENDENT_STUDY` |
| ta'lim natijalari (TN1..) | learning outcomes | `LearningOutcome` |
| joriy nazorat (JN) | continuous assessment, 30 pts | `AssessmentComponent` `CONTINUOUS` |
| oraliq nazorat (ON) | midterm, 30 pts | `AssessmentComponent` `MIDTERM` |
| yakuniy nazorat (YaN) | final, 40 pts | `AssessmentComponent` `FINAL` |
| akademik qarzdor | academic debtor (failed a gate) | derived enrollment flag |

## Rules you must not break
1. **`Subject` ≠ `Course`.** Lessons hang off a `Course` (a subject delivered in a term), never
   a `Subject`. `DAS1110` spans two semesters with different credits and topic lists.
2. **Everything a student owns hangs off `Enrollment`.** No bare `user_id` on progress,
   attempts, submissions or grades.
3. **Never branch on a subject.** No `if (subject.code().equals("DAS1110"))`, no subject-name
   switch, no per-subject class. New subject behaviour = new `ActivityType`/`ContentBlockType`
   + handler. `grep -r 'DAS1110\|IAKTT1105' backend/src/main` must return nothing.
4. **Progress is four-level** (course/module/lesson/activity) and rolls up. Never collapse it
   to one percentage.
5. **Grades are append-only** (ADR-011). Correct by superseding, never by `UPDATE`.
6. **Hours must reconcile.** `course_session_allocations` declares the budget; lesson hours
   should sum to it. `HourBudgetValidator` warns on mismatch (draft courses may be incomplete)
   and blocks on publish.
7. **Assessment components are a tree** and their weights must sum to the scheme total (100).
   `min_pass_percent` gates are real: failing JN or ON at 60% blocks the final; failing 60% of
   the final creates academic debt. Compute it, store the inputs, never guess.
8. **Publication is validated**, not a flag flip: translations complete for `required_locales`,
   no unpublished required child, graded activities bound to a component, hours reconciled.
9. **`required_locales` defaults to `{uz}`** — all supplied material is Uzbek. Never
   auto-generate ru/en filler to satisfy a validator.
10. Independent-study tasks are **variant-based** and 1/3 should be cooperative (`DAS1110`
    footnote). Variants exist; group work is deferred — see PENDING-INPUTS §5-6.

## Adding a practical pattern for a new subject
See `docs/academic-analysis/HOW-TO-ONBOARD-A-SYLLABUS.md` §4. Short version: pick an existing
`ActivityType`; only if none fits, add the enum value + CHECK migration + `ActivityHandler` +
registry test. No core change.
