# Backend requests

What the frontend needs that the API does not offer today. Rule 2 of `../AGENTS.md`: never
fake the missing data — build the rest of the screen and show an honest empty state for the
missing part. Entry format:

```
## <date> — <screen, spec id>
Needs: <what data or action>
Today: <what the API offers and why it is not enough>
Proposal: <METHOD /path — request → response>
Meanwhile: <what the screen shows>
```

## 2026-10-02 — Notifications (S-09), the bell of the top bar — ANSWERED in F3

Answer: `NotificationView.context`, typed (see the API guide §6). The course title is looked up
by `context.courseId`.

Needs: a readable line per notification — what happened and to what ("Grade published:
Dasturlash, JN-1"), and where to go on click.
Today: `NotificationView` carries `type`, `createdAt`, `readAt` and `payloadJson` (a JSON string
whose keys differ by type and are not in the contract). The list can only say "Grade published".
Proposal: add to `NotificationView` the fields `title`, `body` (already localized by the
`locale` parameter) and a typed `target {kind, id, courseId?}`; or document the keys of
`payloadJson` per type in the contract.
Meanwhile: the bell shows a text per notification type and the time; rows do not link anywhere.

## 2026-10-02 — Courses list and dashboard (S-01, S-03.1) — convenience, not blocking

Needs: the course title, the lead teacher and the lesson counts with each enrollment.
Today: `GET /enrollments/me` gives ids and the subject code only, so each course costs two more
calls (`GET /courses/{id}`, `GET /courses/{id}/plan/me`), and finding the course to continue
costs one `progress/me` call per enrollment.
Proposal: add `courseTitle` (by `locale`), `leadTeacherName`, `lessonsTotal`, `lessonsCompleted`,
`continueLessonId`, `lastAccessedAt` to `EnrollmentSummary` for the caller's own enrollments.
Meanwhile: the screens make the extra calls; each card loads on its own.

## 2026-10-02 — Lesson page (S-04) — convenience, not blocking

Needs: the course of a lesson, to open a lesson by its id alone.
Today: `LessonDetail` has `moduleId` but no `courseId`; the frontend keeps the course in the
address (`/student/courses/:courseId/lessons/:lessonId`).
Proposal: add `courseId` to `LessonDetail`.
Meanwhile: lesson links always carry the course id.

## 2026-10-02 — Dashboard on a day without classes (S-01.1) — ANSWERED in F3

Answer: `GET /timetable/next`.

Needs: the date of the next class when today has none.
Today: `GET /timetable/today` returns only today; the next class needs week-by-week calls.
Proposal: add `nextClass` (a `ClassView`) to the answer of `GET /timetable/today`.
Meanwhile: the empty day links to the timetable page.

## Deadlines are not enforced (found in F2, quiz)

- **Screens:** S-05 practice task, S-06 quiz.
- **What happens:** an activity's `dueAt` / `availableFrom` are returned and shown, but
  `POST /quizzes/{id}/attempts` and `POST /submissions` accept work after the deadline (the
  error code `ACTIVITY_NOT_AVAILABLE` exists and is never raised).
- **Needs a decision from the owner, not only code:** block late work, or accept it and mark it
  late for the teacher. Until then the UI shows the deadline as passed and does not block.

