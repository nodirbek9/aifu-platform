# How to onboard a new syllabus

Follow this exactly. It should take **data entry plus zero or one handler**, never a redesign.
If you find yourself editing core tables or core services, stop and record it in
`PENDING-INPUTS.md`.

## 0. Put the original in place (never modify it)
`academic-materials/syllabi/<original-filename>` — as supplied, original name, no edits.

## 1. Extract text for reading
```bash
libreoffice --headless --convert-to txt \
  --outdir academic-materials/_extracted "academic-materials/syllabi/<file>"
```

## 2. Fill the extraction worksheet
Copy `TEMPLATE-subject-extraction.md` to
`docs/academic-analysis/subjects/<SUBJECT_CODE>.md` and complete every field. Mark anything
absent from the document as `NOT SPECIFIED` — do not fill gaps from memory or from another
subject.

## 3. Map to existing structures (in this order)
| Document element | Target |
|---|---|
| subject code, department, type, language | `subjects` + `subject_translations` |
| knowledge/education area, programme codes, ordinal | `academic_programmes`, `subject_programmes` |
| prerequisites (coded or prose) | `subject_prerequisites` |
| `TN1..TNn` + category | `learning_outcomes` (+ translations) |
| year / semester / credits / hours / weekly load | `terms`, `courses` |
| hour budget per session type | `course_session_allocations` |
| lecture / seminar / practical / lab / independent topic lists | `lessons` (`session_type`, `allocated_hours`, `position`) |
| grouping of topics | `course_modules` — mark `grouping_source = 'PLATFORM_INFERRED'` unless the doc groups them |
| rubric (JN/ON/YaN, sub-weights, pass gates) | `assessment_components` |
| grade bands | reuse `grade_scales.code = 'AIFU_100_5'` unless the doc differs |
| literature, web links, benchmark courses | `course_materials` |
| teacher(s), office hours, reviewers, approval protocol | `teacher_profiles`, `course_teachers`, `syllabus_revisions` |

## 4. Decide the practical pattern
For each practical/independent topic, pick the `activity_type` that fits:
`QUIZ` · `CODING_TASK` · `MANUAL_TASK` · `CASE_STUDY` · `PRESENTATION` · `GROUP_PROJECT` ·
`FILE_SUBMISSION`.

**Only if none fits** add a new type:
1. add the value to the `activities.activity_type` CHECK constraint in a **new** migration;
2. add it to `ActivityType`;
3. implement `ActivityHandler` in `curriculum.app.activity.handler`;
4. register it (Spring picks up the bean; `ActivityHandlerRegistry` validates full coverage);
5. add a handler unit test + an `ActivityTypeCoverageTest` entry.

Never add a subject-specific branch. Never add a `subject_code` check in code.

## 5. Load as data
Content is created through the API (`/api/v1/admin/...`, `/api/v1/teacher/...`) or, for
dev fixtures, a seed file in `db/seed/`. **Production academic content is never a Flyway
migration in `db/migration/`.**

## 6. Verify before publishing
```bash
cd backend && ./mvnw -pl api test
# then, against a running instance:
POST /api/v1/teacher/courses/{id}/validate-publication
```
The validator checks translation completeness, hour reconciliation, component weight totals,
required-activity readiness, and orphaned content.

## 7. Record what you assumed
Append an "Assumptions & open questions" section to the subject worksheet and add anything
that needs AIFU confirmation to `PENDING-INPUTS.md`.
