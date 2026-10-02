# Frontend integration fixes (2026-09-26)

Backend changes made while wiring the teacher quiz editor in `aifu-learning-platform-frontend`.
Kept to the smallest change that unblocks the documented API; no contract changed.

## Answer-option update and translation returned 500

**Endpoints:** `PATCH /api/v1/teacher/options/{optionId}` and
`PUT /api/v1/teacher/options/{optionId}/translations/{locale}` (`AdminQuestionController`).

**Defect.** Both handlers called the service first and ran
`accessGuard.requireCourseEditor(option.getQuestion().getQuiz().getCourse().getId())`
afterwards, outside the service transaction. The option's `question` is a lazy proxy, so the
navigation threw `LazyInitializationException` and the request failed with 500 after the write
had already committed. Two consequences:

1. A teacher could never mark an option correct or set its text through the API (always 500).
2. The course-editor check ran after the mutation, so a caller without editor rights on the
   course could still change the option before the request failed.

A frontend-only workaround was not possible: the only other way to change correctness is
delete + re-create, which loses the option's other translations.

**Change.**

- `QuestionService.courseIdOfOption(UUID)` — new `@Transactional(readOnly = true)` lookup of
  the option's course id.
- Both handlers now call `accessGuard.requireCourseEditor(questions.courseIdOfOption(optionId))`
  **before** the write. Response bodies are unchanged.

**Verified** against a stack built from this source (`docker compose -p aifu-next`, ports
8081/5434): a quiz with single-choice, true/false and short-answer questions was created,
published, taken by a student and graded 100% through the UI.

## Second pass (2026-09-27)

### Deleting options and answer keys had no course check

`DELETE /api/v1/teacher/options/{optionId}` and `DELETE /api/v1/teacher/answer-keys/{keyId}`
ran without `requireCourseEditor`: any account with `QUIZ_MANAGE` could delete them in any
course. Both now check first (`QuestionService.courseIdOfOption` / new
`courseIdOfAnswerKey`, read-only). Verified: a teacher of another course gets
403 `NOT_COURSE_TEACHER`; the course's own teacher still gets 204.

### Attached coding-task languages could not be edited or removed

`LanguageTeacherView` did not carry the id that
`PATCH/DELETE /api/v1/teacher/coding-task-languages/{taskLanguageId}` require, so no client
could call them. Added `UUID taskLanguageId` as the last record component (additive: existing
clients ignore it). Once callable, `PATCH` failed with `LazyInitializationException` (500)
while mapping the lazy `ProgrammingLanguage` after the transaction; the handler now re-reads
the language through the existing `listLanguages` query before mapping.

### Uploads always failed with STORAGE_UNAVAILABLE (503)

`docker/api/Dockerfile` runs the API as the non-root `aifu` user, but the `api-storage`
named volume mounted at `/var/lib/aifu/storage` was created root-owned, so every upload
failed. The image now creates that directory owned by `aifu`, which Docker copies into a new
named volume. **An existing volume keeps its old owner**; fix it once with
`docker compose exec -u 0 api chown -R aifu:aifu /var/lib/aifu/storage`.

### Checks run

- `./mvnw -pl api spotless:check` — passes.
- `./mvnw -pl api test -Pit` (Testcontainers) — 223 tests, 222 pass. The one failure,
  `ProgressIT.completingALessonTwice_doesNotDoubleCount`, is unrelated to these changes and
  environment-dependent: on Linux the JDK clock has nanosecond precision
  (`…17.446116281Z`) while Postgres stores microseconds (`…17.446116Z`), so the equality
  assertion fails; on a machine whose clock ticks in microseconds it passes.

## Still open

- `mustChangePassword` is reported by `/auth/me` but not enforced on other endpoints; the
  frontend shows a change-password screen instead.
- There is no student-facing endpoint for coding-task hints or for teacher feedback
  (`/teacher/feedback` is teacher-only), so neither is shown to students.
- The server's allowed upload types include no audio format, so AUDIO blocks cannot be created.
