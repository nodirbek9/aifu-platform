# Frontend build — progress

The single record of progress (AGENTS.md §8). A new session resumes from this file alone.

| Phase | What | Status |
|---|---|---|
| F0 | Foundation: design system, shell, auth | **reported** 2026-10-02 (the owner asked to go on to F1 without waiting) |
| F1 | Student: dashboard, courses, lesson | **reported** 2026-10-02 |
| F2 | Student: practice task, quiz, work list | **reported** 2026-10-02 |
| F3 | Student: timetable, grades, attendance, the rest | **reported** 2026-10-02 |
| F4a | Teacher: timetable, class session, journal | **reported** 2026-10-03 |
| F4b | Teacher: authoring, review, gradebook, analytics | **reported** 2026-10-03 |
| F5a | Admin: reference data, users, structure, audit | **reported** 2026-10-03 |
| F5b | Admin: timetable builder | **reported** 2026-10-03 |
| F6 | Polish, motion, accessibility, e2e, packaging | **reported** 2026-10-04 |

Who built what: F0 step 1 and the first half of step 2 (scaffold, API layer, tokens, i18n,
`shared/ui`, auth pages) by Kimi K3; from the entry point on (router, guards, shell, system
pages, tests, the fixes below) by Claude, at the owner's request when Kimi's quota ran out.

## F0 — report

Spec IDs done: G-01 (three languages, switch always visible, saved to the account), G-03 (the
four states exist as components; pages of later phases use them), G-05 (light / dark / system,
remembered, no flash), G-08 (login, forced password change, silent single-flight refresh, logout,
logout everywhere, return to a deep link), G-09 (role-aware navigation, role switch, "no access").
G-02, G-06, G-07 are met by the screens that exist; they are re-checked per screen in each phase.

Verified (run, not assumed):
- `npm run lint`, `typecheck`, `test` (23 tests), `i18n:check` (151 keys × 3), `build` — all pass.
- `npm run e2e` against the running backend: 26 passed (13 flows × desktop 1440 and phone 390):
  sign-in as student3, admin and n.ayitbayev; role switch and its memory; wrong credentials;
  empty fields; reload keeps the session; deep link returns after login; sign-out; a student at
  an admin address sees "no access"; unknown address; three languages on the login page; the
  language is saved to the account; the theme is remembered.
- Screenshots (`npm run shots`) of login, the shell in each role, "no access", "not found" and
  `/dev/ui` at 1440 and 390, light and dark, uz / ru / en — looked at; what they showed was fixed
  (see DESIGN-DECISIONS.md §10).

Not verified / not done:
- Forced password change: built, not run end to end — no demo account has `mustChangePassword`.
- "Log out everywhere", the session-expired path, the offline banner and the route error page:
  built, exercised by hand only in part, no automated flow.
- Tablet (768) screenshots were not reviewed.
- The first bundle is 863 kB (270 kB gzip): pages are not lazy yet. Planned for F6; pages added
  from F1 on are lazy routes.

Found and fixed while finishing (Kimi's part had never been compiled as a whole):
- about 15 type errors; `throwOnError` of openapi-fetch was misread (now `unwrap()`);
- ESLint config did not run at all (typed rules applied to plain JS files);
- the i18n checker reported every dynamic key as unused (prefix bug) and its test imported a
  wrong path; the password form enforced 8 characters where the server requires 10;
- `tailwind-merge` dropped text colours next to type-scale classes (white labels turned dark);
- two npm packages had been installed into the repository root by mistake.

Backend requests added: notifications need a title, a body and a target (BACKEND-REQUESTS.md).
Backend defects found and fixed in the backend (commit `2ff828b`): sign-in limit of 10 a minute
per address (a campus shares one address), `requestId: "null"` on 401 / 403, a 500 on an
`X-Request-Id` with stripped characters.
Design decisions recorded: DESIGN-DECISIONS.md §10.

## F1 — report

Spec IDs done:
- S-01 dashboard: today's classes with the current / next one marked (S-01.1), continue learning
  (S-01.2), the next five deadlines with overdue in red (S-01.3), recent grades (S-01.4), the
  name and group as the headline, four blocks that load and fail independently (S-01.5).
- S-03 courses: the list with code, teacher, term, progress and next lesson (S-03.1); the course
  page with Plan / Syllabus / Work / Grades, the tab in the address (S-03.2); "Continue" (S-03.3).
- S-04 lesson: reading column, table of contents with scroll-spy (rail on desktop, drawer on a
  phone), reading progress line, previous / next (S-04.1); every block type of the API guide §5
  (S-04.3); "Mark as done" saved on the server and shown in the plan (S-04.5); language switch
  that keeps the text and the position, with a fallback note (S-04.6); the locked state.

Verified (run, not assumed):
- lint, typecheck, 39 unit tests, i18n (257 keys × 3), build — pass.
- `npm run e2e`: 36 passed (18 flows × desktop and phone) against the real backend, including:
  dashboard blocks filled with real data; courses → course → syllabus → work, tab kept on
  reload; continue → lesson → mark as done → the plan shows it; language switch on the lesson;
  a lesson that does not exist.
- Screenshots of the dashboard, courses, course (plan, syllabus, work) and two lessons at 1440
  and 390, light and dark, plus Russian — looked at.

Not verified / not done:
- S-01.1 "next class date" on a day without classes: the block links to the timetable instead;
  the date comes with the week view in F3.
- S-04.2 syntax highlighting of code examples and S-04.4 "Try it": with the code editor, in F2.
- S-04.2 self-check "click to reveal": the content has no such structure yet (the questions are
  a callout); to be settled with the content format.
- IMAGE / VIDEO / AUDIO / FILE / EMBED blocks are built from the contract but the demo database
  has none (one DIAGRAM), so they were not seen with real data. Tablet width was not reviewed.
- The locked-lesson state was not seen: no lesson of the demo data has prerequisites.

Found and fixed: Chromium has no Uzbek calendar data (October printed as "M10") — Uzbek dates
are now written by hand in `shared/lib/dates.ts`.
Backend requests added: none that block; three conveniences are listed in BACKEND-REQUESTS.md.
Demo data: the e2e global setup clears student3's lesson progress before each run (the API does
not let a student un-complete a lesson).

## F2 — report

Spec IDs done: S-05.1 – S-05.8 (practice task), S-04.4 ("Try it"), S-06.1 – S-06.4 (quiz),
S-07 (all my work).

Built:
- `shared/editor`: CodeMirror 6 for Python, Java and C++ — highlighting from the design tokens
  (follows light / dark by itself), auto-indent, bracket matching, line numbers, four-space tab,
  font size, read-only mode. A separate chunk (607 kB), loaded only where code is shown.
- S-05 practice task (`/student/courses/:courseId/tasks/:taskId`, a focus page): statement,
  input / output, constraints, limits, examples with copy, hints opened one at a time; language
  tabs with each language's starter code, a draft per task and language that survives reload and
  language switch, reset with confirmation; font size, full screen, shortcuts on the buttons;
  Run with custom input; Run examples with input / expected / actual for a failed one; Submit
  with an idempotency key, live queued → running status, the verdict reveal, hidden tests shown
  without data; history, opening an attempt read-only and restoring it, attempts left and the
  deadline; the teacher's feedback. Resizable panes on desktop; Task / Code / Result tabs with
  pinned actions on a phone.
- S-04.4 "Try it": a lesson example opens in a runner in place; lesson code is highlighted.
- S-07 Assignments: one list across courses, filters by course, type and state kept in the
  address, sorted by deadline.
- S-06 Quiz (`/student/courses/:courseId/quizzes/:quizId`, a focus page; `features/quiz`):
  - start screen: description, time limit, attempts used / allowed, pass mark, points, deadline,
    the rules (including what the teacher's review policy will show), the earlier attempts with
    their scores; starting a timed quiz asks first — the clock starts on the confirmation;
  - the attempt: one question per screen, a numbered navigator (answered / unanswered / current /
    marked "come back later"), Previous / Next, the position kept in the address (`?q=`);
  - all seven question types of the API: single choice, multiple choice, true / false, numeric
    (comma or dot; what is not a number is said to be so and is not sent), short answer,
    ordering (up / down buttons, announced to screen readers; an untouched order can be
    confirmed as it stands), matching (press two items to pair them; a pair can be split);
  - answers saved as the student goes — at once for a choice, after a pause for typing — one
    request at a time per question with the newest value winning; "Saving… / Saved / Not saved +
    Retry" in the header;
  - the clock runs on the server's time (`serverTime`), turns to the warning colour in the last
    two minutes and to red in the last thirty seconds, is announced at five minutes and at one;
    at zero the attempt hands itself in;
  - resume: reload returns to the same question with the answers in place; the quiz's own
    address goes straight into an open attempt; leaving asks first and says the clock keeps
    running;
  - finishing: a confirmation with answered / total, the unanswered questions as links, the
    time left; unsaved answers are flushed first and block the hand-in if they cannot be saved;
  - result: score (counted up once for a fresh result, instantly under reduced motion), percent,
    passed / not passed against the pass mark, "closed by time" when the clock handed it in;
    the review when the policy opens it — per question the verdict (right / partly / wrong /
    not answered), points, the right options, the right order, the right pairs, an accepted
    typed answer, the explanation; otherwise a note saying when the review opens; the teacher's
    feedback on the attempt.

Backend changed for this phase (by Claude; see `docs/03-API-GUIDE.md` §4 and §8):
- An attempt whose time ran out used to become `EXPIRED` with no score: every saved answer was
  lost. Now the clock hands it in and it is graded like any submission.
- Options were never mixed, although quizzes say `shuffleOptions`; ordering questions were
  served in the right order. Options are now mixed per attempt, stably.
- The review told only "right / wrong". It now also tells the right options, order, pairs and
  an accepted typed answer — only when the review policy opens it.
- Ordering answers are graded by sequence, not by raw position numbers the client cannot know.
- `AttemptDetail.serverTime` for the countdown.
- Demo data: quiz `DAS-Q-M2` in the Dasturlash course with all seven question types in uz / ru /
  en (seed `V909`), bound to lecture M2 with a deadline and three attempts.

Verified (run, not assumed):
- `npm run lint`, `typecheck`, `test` (67 tests), `i18n:check` (437 keys × 3), `build`.
- `npm run e2e`: 54 passed (27 flows × desktop and phone). New for the quiz: every question
  type answered, reload mid-attempt, finish, result 9 / 10 with the review; leaving and
  resuming, marks, the unanswered list, result 1 / 10; the time running out with nobody
  pressing anything (the flow moves the attempt's end to eight seconds from now in the
  development database) → handed in, 3 / 10, "closed by time"; switching the language
  mid-attempt keeps the answer.
- Code really runs in the sandbox in the coding flows: a correct Python solution run, checked
  on the examples, submitted and accepted; a wrong answer; a Java compilation error; a hint;
  "Try it" in a lesson; the work list filters.
- Backend: `AssessmentIT` (12 tests, three new: time-up grades what was saved; the next start
  closes a stale attempt; ordering options are mixed, stable and graded by order),
  `OpenApiContractIT`, `DevSeedIT`.
- Screenshots of the quiz (start, each kind of question, the finish dialog, the result, the
  start screen after an attempt) at 1440, 768 and 390, light and dark — looked at.

Not verified / not done:
- A solution in Java and in C++ submitted through the UI (acceptance step 4): only Python is
  submitted by the flow; Java is exercised by a run (compile error). The backend proved all
  three end to end; the UI path is the same code.
- The 429 (rate limit) and 422 (attempt limit) messages of the task page: built, not provoked.
- The quiz with a review policy other than "after the attempt" (never / after the last attempt /
  after the deadline): the "review is closed" note is built from the same absence of data the
  unit tests cover, but no demo quiz with such a policy was walked through in the browser.
- A quiz without a time limit, and a quiz whose attempts are used up: built, not walked through.
- "Not saved + Retry" (the network failing mid-attempt): built, not provoked in a flow.
- The teacher's feedback on an attempt: built; no demo attempt has feedback.
- Quiz question prompts are plain text (the API has no rich text or code formatting for them).

Demo data: the quiz flows delete student3's attempts at `DAS-Q-M2` before each test; the e2e
global setup also clears student3's lesson progress and submissions to DAS-S1-A8.

## F3 — report

Spec IDs done: S-02.1 – S-02.5 (my timetable), S-08 (grades), S-09 (notifications, search,
profile), S-10 (my attendance); S-01.1 (the next class on an empty day) and announcements.

Built:
- S-02 My timetable (`/student/timetable`, `?date=` keeps the week): the week as a grid of days
  × pairs with the bell times, class cards tinted by session type with room and teacher, today's
  column tinted, the "now" line across the current pair; week switch (previous / next / this
  week), the dates, the week number and toq / juft as a chip — all from the server; holidays,
  cancelled classes and both slots of a moved class (struck through with the reason; the new
  slot marked); a legend of the session types of the week. Phone: a day strip and the day's
  list. The class sheet: time, pair, room, teacher, group, what changed and where the class
  went or came from, the topic with "open the lesson", and the course.
  `WeekView` and `shared/ui/ScheduleGrid` are built to be reused by the teacher's timetable and
  the admin's builder.
- S-08 Grades (`/student/grades`, and the Grades tab of a course): per course the running total
  out of 100, the admission to the final as a clear statement, the JN / ON / YaN scheme with
  its parts — earned / maximum, a bar, the pass mark and whether it is reached — the grades
  that count, and the history of changes (a corrected mark stays, struck through). The final
  grade is shown only once every component is graded.
- S-10 My attendance (`/student/attendance`): per course the percentage, held / present / late /
  excused / absent, every class with its date, pair, topic and mark; "missed only" filter kept
  in the address.
- S-09 Notifications: the bell (unread count, the newest six, "mark all read", a link to all)
  and the page `/notifications` (all / unread, pages, mark all read). Each notification says
  what happened and to what — the announcement's title, the course, the dates of a moved class,
  the new total — and opens it; opening marks it read.
- S-09 Search: the command palette (Ctrl+K; a search button on a phone) asks the server as the
  word is typed and lists my courses and lessons under the commands.
- S-09 Profile (`/profile`): name, roles, login, group, student number, email; phone (editable);
  language and theme; change password (the same form as the forced change).
- Announcements: `/announcements`, a tab on the course page, and the newest two on the
  dashboard.
- Dashboard: an empty day names the next class; recent grades name the graded work.

Backend changed for this phase (by Claude; `docs/03-API-GUIDE.md` §4, §6, §8):
- Grades: `GET /enrollments/{id}/grades` now carries the scheme with earned points and gates,
  and the history. The final score calculator took the *last* entry of a component when there
  were several (five graded tasks → the fifth alone); it now weighs them by their maximums.
- Search matched whole words only ("dastur" found nothing) and only in the reader's language;
  it searched all published courses, including those the student cannot open, and no lessons.
  It now matches prefixes in every language, covers lessons, and is limited to the caller's
  own courses (an administrator sees everything).
- Notifications: a typed `context` in the contract; the grade notification carries its course.
- `GET /timetable/next`.
- Demo data (seed `V910`): the Dasturlash course got the JN / ON / YaN scheme; student3 got
  grades (one corrected) and notifications.

Verified (run, not assumed):
- `npm run lint`, `typecheck`, `test` (93 tests), `i18n:check` (527 keys × 3), `build`.
- `npm run e2e`: 72 passed (36 flows × desktop and phone). New: the weeks named in the phase
  prompt — 19 October (moved lecture, with its sheet), 12–16 October (cancelled practical, odd
  week), 1 October (holiday), and xolmatov.u's odd / even subgroups; grades by scheme with
  admission and history, also on the course page; attendance with the missed filter;
  notifications (unread count, opening one from the page and one from the bell, mark all
  read); search by a typed prefix leading to a lesson; announcements in three places; the
  profile saving a phone number; the next class on an empty day.
- Backend: `./mvnw clean verify -Pit -Dsandbox.smoke=true`.
- Screenshots of every new screen at 1440 and 390, light and dark, and at 768 in light; the
  timetable also with the "now" line (the clock set to a Friday morning) — looked at.

Not verified / not done:
- Changing the password from the profile: the form is the one the forced change uses (covered
  by unit-level review only); running it would change the demo account's password for everyone.
- Tablet width (768): the timetable and the dashboard were looked at (long subject names now
  break inside a class card); the other F3 screens were captured, not reviewed one by one.
- A week with a class on Sunday, a class outside the bell schedule, a `REPLACED` class (other
  room or teacher): handled in code and unit tests, no demo data has them.
- Notifications of the types the backend does not send yet (deadline reminder, quiz graded,
  account status): they show their type's text and link where a target is known.
- Notification and search hits of a teacher or an administrator lead nowhere yet: their pages
  arrive in F4 / F5 (`notificationPath`, `searchHitPath`).
- The print stylesheet of the timetable belongs to T-02 (F4a).
- Search does not look inside lesson text, only titles and summaries.

## F4a — report

Spec IDs done: T-01, T-02, T-03.1 – T-03.5, T-04.3, T-10, T-11.

Built (`features/teaching`):
- T-01 Teacher dashboard (`/teacher`): today's classes with "Start class", the next class on an
  empty day, the courses, the students falling behind per course (names from the roster),
  the work that closes this week.
- T-02 My timetable (`/teacher/timetable`): the shared week grid with groups and rooms, the
  pairs and hours of the week, toq / juft; a class card opens its class page; a print button
  and a print stylesheet (A4 landscape, no navigation).
- T-03 Class page (`/teacher/classes/:entryId/:date`): header (subject, type, date, time, room,
  groups, what changed); attendance as a fast list — four states per student, "All present",
  each tap shown at once and saved in order (a failure undoes the tap and says so), counts that
  follow; the topic: the suggested next lesson linked in one tap, any lesson of the course from
  a list, or a free-text topic; private notes saved as typed; the teacher-only lesson plan
  (objectives, timing, materials, questions, homework).
- T-03.3 Presentation mode (`/teacher/lessons/:lessonId/present`, a focus page): dark, very
  large (zoom 1.25–2.25), one section per screen, arrows / Page Up / Down / Space / Home / End,
  F for full screen, Escape to leave, on-screen arrows and a progress bar.
- T-04.3 Calendar-thematic plan (a tab of the course page): every class of the term with its
  week, date, type, group and topic; cancelled and moved classes marked; a row of my own
  classes opens its class page where the topic is set.
- T-10 Attendance journal (`/teacher/journal` and a tab of the course): students × the classes
  whose attendance was taken, sticky header and first column, totals in %, below 75 % in red,
  filter by group and period, export to Excel. Groups (`/teacher/groups`): the roster of each
  group, exportable to Excel. The course's students tab: completion and failed attempts.
- T-11 Announcements (a tab of the course): post to the course or to a group, the list, delete
  with confirmation.
- `shared/lib/xlsx.ts`: a small .xlsx writer (no library); the downloaded journal was opened
  with openpyxl to confirm it is a valid workbook. `shared/ui/stage.tsx`: magnified content.
- Fixed on the way: a select with a long value widened its whole column (on a phone the page
  overflowed); it now truncates.

Verified (run, not assumed):
- `npm run lint`, `typecheck`, `test`, `i18n:check`, `build`; `npm run e2e` (the full suite).
- New flows (desktop and phone): the week with its hours → a class from its card → all present,
  one absent → link the suggested lesson, a note → reload keeps everything → presentation mode
  moves by keyboard and button and Escape returns → the journal shows the absence; the journal
  downloads as .xlsx; an announcement is posted and deleted.
- Screenshots of every F4a screen at 1440 and 390, light and dark — looked at.

Not verified / not done:
- Printing was not sent to a printer; the print stylesheet hides the navigation and sets A4
  landscape.
- "Submissions waiting for review" on the dashboard (T-01) arrives with the review queue of F4b.
- The class page of a class taught by someone else (a lecture in the plan): linked since 2026-10-04 (any teacher of the course may open it, as the API allows).

## F4b — report

Spec IDs done: T-01 (submissions waiting for review), T-04.1, T-04.2, T-05, T-06, T-07, T-08, T-09.

Plan as built: the course page gets five more tabs — Content, Practice, Review, Grades,
Analytics (`teacher-course-route.tsx`); the editors are pages of their own.

Built:
- `features/authoring` — T-04.1 curriculum tree (`?tab=content`): modules and lessons in order;
  create, rename (every language), reorder by drag or Alt+↑/↓ on the handle (optimistic, with an
  announcement for screen readers), publish / back to draft, delete with confirmation; the hours
  of the lessons set against the syllabus' hour plan per kind of class (a warning lists every
  kind that differs). T-04.2 lesson editor (`/teacher/lessons/:id/edit`): the lesson's titles per
  language, kind, hours, reading time; blocks of every type — text, callout, code example (the
  code editor), table, case, image / diagram / video / audio / file (upload through POST /files),
  YouTube / Vimeo, link, references to a quiz / task / assignment of the course, and the
  teacher-only lesson plan (objectives, a timed list of steps, materials, questions, homework);
  UZ / RU / EN tabs on every block with a dot where a language is missing (amber where the course
  requires it); add / reorder / delete; beside it the lesson exactly as the student renderer
  shows it, in any language, with the fallback mark. Everything saves itself after a pause; the
  state is always shown; a 409 says "changed elsewhere" with a reload button.
- T-05 coding task editor (`/teacher/coding-tasks/:id/edit`): statement per language, starter
  code and reference solution per language (Python, Java, C++) side by side, tests (input,
  expected output, example / hidden, comparison, weight), limits and scoring, hints with their
  penalty per language, and "Verify solutions" (POST, then polled) with the result per language
  and per test. T-06 quiz editor (`/teacher/quizzes/:id/edit`): texts per language, settings
  (time, attempts, pass mark, reveal policy, shuffling), questions of all seven types — options
  with the right one(s) marked, true / false made for you, ordering in the right order, matching
  as pairs, short answers and numbers as answer keys — reorder, delete; publishing shows the
  server's reason when it is refused. The Practice tab lists both and creates new ones.
- `features/review` — T-07: the course's queue (task, verdict, group, "without feedback", "latest
  attempt") and a submission page: the code read-only, every test with input / expected / output
  (hidden ones too), the feedback so far and a form (student-visible or internal), and a grade
  for a component of the gradebook, suggested from the automatic score. T-01: the dashboard counts
  the submissions waiting per course.
- `features/gradebook` — T-08: students × the components of the scheme, final score with its
  grade, admission and debt from the server; filter by group; arrows move between cells, Enter
  opens one: record a grade, void one with a reason, the history of the component (nothing is
  erased); Excel export.
- `features/analytics` — T-09: the completion funnel, lesson completion, coding-task acceptance,
  quiz results with an item analysis of the chosen quiz, the students falling behind. Each chart
  (`shared/ui/bar-chart.tsx`, token colours) has a one-sentence summary and a table, which
  assistive technology always reads and anyone can switch to.
- Backend changes it needed (with tests): editor views carry every translation (tasks, hints,
  quizzes, questions, options, blocks); `GET /teacher/courses/{id}/submissions` (the queue);
  `GET /teacher/courses/{id}/gradebook` (with the group); deleting an announcement withdraws its
  notifications; an unknown path answers 404 instead of 500.

Verified (run, not assumed):
- `npm run lint`, `typecheck`, `test`, `i18n:check`, `build`; the full `npm run e2e`.
- New flows (`e2e/teacher-authoring.spec.ts`): a module and a lesson created, a block written in
  Uzbek and Russian and seen in the preview in both, a block moved by keyboard, publish and back,
  lesson and module deleted; a coding task with a statement, a Python solution and a test passes
  the verification in the real sandbox; a quiz refused while empty (the reason shown), then a
  question with options, the right one marked, published and back; **acceptance step 6** — a
  student's submission reviewed, commented and graded, and the student sees the comment on the
  submission and the grade in Grades; the gradebook moved through by keyboard, a grade voided with
  a reason and kept in the history, exported; the analytics with their tables; every tool fits a
  phone screen.
- Screenshots of the F4b screens at 1440 and 390, light and dark — looked at.

Not verified / not done:
- Rich text is edited as HTML with a formatting toolbar and a live preview, not a WYSIWYG editor.
- Quiz deadlines live on the course's assignments (activities), not on the quiz; the quiz editor
  has no deadline field.
- Lesson prerequisites and learning-outcome links have API endpoints but no editor yet.
- The hour-budget check is made in the browser from the syllabus plan and the lessons' hours; the
  server only reconciles the plan with the course total when the course is published.

## F5a — report

Spec IDs done: A-01, A-03, A-04, A-05, A-06, A-07.

Plan as built: one list → detail → form pattern (`features/admin`): lists keep search, filters
and page in the address; forms open in a dialog or on a detail page; anything destructive asks
first and says what it touches.

Built:
- A-03 Reference data (`/admin/reference`, a tab each): groups (search, archived shown on
  demand, create / edit with programme, year, study form, language, planned size and curator; a
  side sheet with the roster — add students by search, remove — archive / restore, delete);
  rooms (building, number, capacity, type, active); the bell schedule (edit a pair's times, add the
  next pair 10 minutes after the last, delete); holidays; academic years and their terms (make a
  year current, add terms) with the first week of each term set odd or even.
- A-05 Users (`/admin/users`): search, role and status filters, paging; a new user (a student
  with number and group); a user page — details, roles, status with a reason, unlock, a new
  password. Import (`/admin/users/import`): an .xlsx or .csv read in the browser
  (`shared/lib/spreadsheet-read.ts`, no library; columns found by any of their spellings in three
  languages), a downloadable template, a dry run whose outcome is shown row by row, then the
  import.
- A-06 Structure (`/admin/structure`): subjects (create; a subject page with its texts per
  language, prerequisites, learning outcomes, publish / archive) and courses (create from subject
  and term; a course page with texts per language, status, teachers and their role, the hour
  plan per kind of class with the check against the total, the assessment scheme with its parts,
  required languages, and enrollment — one student by search or a whole group at once).
- A-04 Teaching load (`/admin/load`): lines per term with planned, scheduled and remaining hours;
  a line by hand; the department's sheet imported (subject code, group, hours per kind, teacher —
  unmatched rows listed with the reason); a view per teacher.
- A-07 Audit (`/admin/audit`): action, entity and date filters in the address; the roles with
  their permissions.
- A-01 Dashboard (`/admin`): students, teachers, groups, active courses; today's classes of the
  published timetable; unscheduled load and classes saved over a warning; recent events without
  the sign-in noise.
- Shared: `PageHeader` actions wrap on a phone; `Tabs` never widen their parent.

Verified (run, not assumed):
- `npm run lint`, `typecheck`, `test` (the reader also against a workbook written by openpyxl),
  `i18n:check`, `build`.
- Flows (`e2e/admin-records.spec.ts`): a group, a room and a holiday are created (a holiday on a
  date that already has one is refused and the reason shown); three students imported from a
  file — the dry run saves nothing, the import creates them in their group; every admin screen
  fits a 390 px phone.
- Screenshots of the F5a screens at 1440 and 390 — looked at; the scheme now lists each part
  under its own component.

Not verified / not done:
- Enrollment by CSV upload (`/admin/enrollments/import`) has no screen; by group and by search do.
- A group's students are found by search; the bulk assignment is the import (a known student
  number with another group moves the student).
- The e2e suite runs against the Vite dev server, which now and then reloads a page while it
  optimises a newly loaded module: one flow in a long run may time out on the sign-in form and
  pass on its own. F6 runs the flows against the production build.

## F5b — report

Spec IDs done: A-02.1 – A-02.8.

Plan as built (`features/builder`): the state lives in the address — `tt` (timetable), `view`
(group / teacher / room), `id` (which one), `parity` — so a link opens the same grid; the panel,
the drag and the last undo are local. Keyboard model: a class card is a button (Enter opens it);
Space lifts it, the arrows move it cell by cell (to the nearest cell in that direction, measured
from the cell it is over), Space drops it, Escape puts it back. 1440: the grid (days × pairs)
with the load panel beside it, the class panel as a side sheet. 390: a list of the days with an
edit sheet (read-mostly, as the brief asks).

Built:
- A-02.1 Timetables: choose one (draft / published / archived beyond doubt: a badge and, when
  published, a read-only notice), a new draft (empty or a copy of another), a draft copy of the
  published one, rename, publish (confirm, says the term's previous one is archived), archive,
  delete a draft.
- A-02.2 One grid seen by group, teacher or room; every / odd / even week.
- A-02.3 / A-02.4 The class panel: course of the term, kind of class (those with hours in the
  course), teacher (from the course's teachers), one or more groups, room (type and capacity
  shown), day, pair, weeks, dates, subgroup, note. While it is filled in, the server's check
  answers: a clash names the other class (teacher / room / group busy, with its course, teacher,
  groups, room, day and pair) and blocks saving; a warning (room too small, wrong room type…)
  can be overridden with a typed reason.
- A-02.5 Drag and drop (dnd-kit): the class lifts, the cell under it turns green / amber / red as
  the check answers, a refused drop shakes the card once and says why (toast and live region), a
  drop onto a warning opens the panel for the reason; the last change (add, edit, move, delete)
  can be undone.
- A-02.6 The load panel: per course and kind of class, planned against scheduled hours.
- A-02.7 One-off changes of a class for a date (cancel, move to a date and pair, another room)
  with a reason; listed and deletable; a week preview of any date as students and teachers see it.
- A-02.8 Print (only the grid and its title) and Excel of the grid on screen.
- Fixed on the way: the shared `Select` passed on an empty value Radix reports when its options
  arrive late — a dialog could send an empty id (the new draft did). Read-only class cards no
  longer carry dnd-kit's aria-disabled.

Verified (run, not assumed):
- `npm run lint`, `typecheck`, `test`, `i18n:check`.
- **Acceptance step 1** (`e2e/admin-builder.spec.ts`, desktop): two groups and a computer room;
  the bell schedule is there; a new empty draft; group A gets Dasturlash on Monday, pair 1;
  group B at the same time with the same teacher is refused with the clash named and saving
  blocked, pair 2 fits; group A's class moved by keyboard onto pair 2 is refused (the teacher is
  busy), moved to Tuesday it goes and the move is undone; the draft is published. The demo
  timetable is restored afterwards. On a phone the week reads as a list of days and fits 390 px.
- Screenshots by group, teacher and room, and the class panel, at 1440 and 390 — looked at.

Not verified / not done:
- A one-off "replace the teacher" is not offered (the API takes a new teacher; the panel offers
  a new room); cancelling and moving are.
- Export is Excel only; PDF is the browser's print to PDF of the print stylesheet.
- The load panel shows over-scheduled lines (36 of 30 h) without a separate warning.

## F6 — report

1. Design pass: `DESIGN-REVIEW.md` — ranked findings, what was fixed, what is open. The biggest:
   pages wider than a phone (now swept on every screen), contrast, unnamed controls.
2. Motion: the eight moments of the brief §8 (the dashboard entrance is new); all off under
   reduced motion.
3. Accessibility: axe (WCAG 2.1 AA) on every route of every role, light and dark — **no
   violations** (`e2e/a11y.spec.ts`). Keyboard paths are in the flows: attendance, presentation
   mode, the gradebook grid, the curriculum tree's reordering, the timetable builder's drag.
4. Performance: every page a lazy chunk; the code editor, charts' data, Excel, GSAP, the form
   library, the sign-in page and the Russian and English catalogues out of the initial bundle —
   it fell from 627 kB to 435 kB (134 kB gzipped: React, the router, the query client, the
   shell, Uzbek). Lighthouse on the sign-in page (production build): desktop performance 97
   (FCP 0.8 s, LCP 1.1 s, TBT 30 ms, CLS 0); mobile (slow 4G, 4× CPU) performance about 70,
   accessibility 100, best practices 100, SEO 91. The dashboard and the lesson page load no
   layout shift (CLS 0 measured in the browser); they were not put through Lighthouse, which
   cannot sign in.
5. Resilience (`e2e/resilience.spec.ts`, all pass): the API down — an error with retry, the
   retry brings the page back; a slow network — skeletons, then the page; the session expiring
   in the middle of a form — the token is renewed and the save goes through; two tabs renewing
   the session at the same moment — both stay signed in (Web Locks); a reload while a solution
   is in flight — it is neither lost nor doubled.
6. Acceptance: the six steps of the spec §9 run as flows against the real backend
   (`admin-builder` step 1, `teacher-classroom` step 2, `student-learning` step 3,
   `student-coding` step 4, `student-quiz` step 5, `teacher-authoring` step 6) on the desktop
   and, where the step is meant for it, the phone; in Uzbek. Russian and English: every screen
   of every role is opened on desktop and phone with no untranslated text and nothing wider than
   the screen (`e2e/locales.spec.ts`); the step-by-step flows themselves were not repeated in
   those languages.
7. Packaging: `Dockerfile` (Node build → `nginx-unprivileged`), `nginx.conf` (SPA fallback,
   `/api` to the api service with the same-origin `Origin` dropped, year-long cache for hashed
   assets, `no-cache` for `index.html`, CSP without inline scripts and other security headers,
   55 MB uploads, `/healthz`); built and run against the compose API. The compose service is
   proposed in `BACKEND-REQUESTS.md`, not added.
8. `README.md`: run, test, build, ship; the structure; the decisions.

The flows now run against the production build (`vite preview`), which removed the dev
server's occasional mid-flow reloads.

## Phase 5 — Learning Path UI (F1–F4)

Completed steps F1–F4 of Phase 5 (Learning Path) per `docs/PLAN.md` §3 and `ADR-015`:

- **F1 Model & narrowing**:
  - `features/courses/model.ts`: extended `PlanLesson` (`steps: LessonSteps`, `lockReason?: LockReason`, `waived: boolean`), `CoursePlan` (`pathEnabled: boolean`, `midterm?: MidtermStanding`), narrowing functions `narrowLessonSteps`, `narrowMidterm`, `narrowPlanLesson`, `narrowPlan`.
  - `features/lesson/model.ts`: defined `LessonPath` (`lessonId`, `pathEnabled`, `steps`, `readingSeconds`, `requiredReadingSeconds`, `maxBlockPosition`, `reachedEnd`, `nextAction`), `narrowLessonPath` with safe defaults; preserved `position` on blocks in `narrowBlock` and `groupCode`.
  - Unit tests in `features/courses/model.test.ts` and `features/lesson/model.test.ts`.
- **F2 StepDots & Plan List**:
  - `shared/ui/step-dots.tsx`: 3-circle component (lecture, test, practice) with DONE (success fill + check icon), TODO (outline), NONE (dashed muted); accessible `role="group"` with tooltips and aria-labels in uz/ru/en. Exported from `shared/ui/index.ts`.
  - `features/courses/course-plan-list.tsx`: renders `StepDots` for each lesson row when `pathEnabled: true`, displays lock reasons (`PREVIOUS_LESSON_INCOMPLETE`, `MIDTERM_REQUIRED`), displays waived badges (`Ozod etilgan`), prevents navigation on locked lessons, and renders `MidtermCard` (opensAt, attempts, passed status, start midterm link).
- **F3 Server Lock Gate**:
  - Intercepted 422 `LESSON_PREREQUISITE_NOT_MET` in `features/lesson/lesson-page.tsx`, extracting `reason`, `blockingLessonId`, and `midtermActivityId` to render `LockedLesson` without fetching/rendering lesson content or skeletons.
  - Linked actions to blocking lesson or midterm activity, plus return link to course.
  - Added student activity route `/student/courses/:courseId/activities/:activityId` in `app/router.tsx`.
- **F4 Reading Heartbeat & Steps UI**:
  - `features/lesson/use-reading-heartbeat.ts`: 15 s periodic heartbeat respecting ADR-015 anti-cheat rules (only when `pathEnabled`, `!lectureDone`, tab visible, user active in last 120 s via pointer/keyboard/scroll), tracking `maxBlockPosition` and `atEnd` via sentinel `IntersectionObserver`, flushing immediately on `visibilitychange` to hidden.
  - `features/lesson/use-reading-heartbeat.test.ts`: unit tests using fake timers verifying all guard conditions, inactivity timeout, and visibility flushing.
  - `features/lesson/lesson-blocks.tsx`: rendered `data-block-position={block.position}` on block wrappers and added lesson-end sentinel element.
  - `features/lesson/lesson-page.tsx`: header displays `StepDots` and reading time progress ring/count; footer replaces "Mark done" button with steps status panel and nextAction prompt (`KEEP_READING`, `TAKE_TEST`, `PRACTICE`, `ALL_DONE`) when `pathEnabled: true`.
  - Localization in `uz.json`, `ru.json`, and `en.json` with identical keys and zero ASCII apostrophes for Uzbek Latin script.

- **F7 Learning Path: Administrator's Side**:
  - `features/admin/learning-path-queries.ts`: typed queries/mutations `useLearningPathConfigQuery`, `useSaveLearningPathConfig`, `useAttemptGrantsQuery`, `useGrantAttempt`; pure conversion and validation helpers `isoToCampusDatetimeLocal`, `campusDatetimeLocalToIso`, `validateGrantReason`, and `buildLearningPathConfigRequest`.
  - `features/admin/learning-path-queries.test.ts`: vitest tests for all pure date conversion, reason validation, and request builder helpers.
  - `features/admin/learning-path-page.tsx`: dedicated Admin page at `/admin/learning-path` with course picker, `LearningPathCard` (enable switch with waiver warning confirmation dialog, midterm quiz picker, anchor lesson picker, `midtermOpensAt` campus timezone picker), `ExtraAttemptsSection` (paginated table of grants), and `GiveAttemptDialog` (quiz picker, student enrollment picker, reason input min 5 chars, human error reporting).
  - `features/admin/course-admin-page.tsx`: embedded `LearningPathCard` alongside components and locales; added "Give attempt" action trigger to `Enrollments` student table rows opening `GiveAttemptDialog` prefilled with the student.
  - `app/shell/nav.ts` & `app/router.tsx`: added `learningPath` (`Route` icon) to Admin nav and lazy-loaded `/admin/learning-path` route.
  - `shared/i18n/{uz,ru,en}.json`: added `nav.learningPath` and `admin.learningPath.*` catalogues across uz, ru, and en with exact key parity and correct Uzbek Latin modifier letters (U+02BB `ʻ`, U+02BC `ʼ`).


