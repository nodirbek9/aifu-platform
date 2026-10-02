# Frontend build — progress

The single record of progress (AGENTS.md §8). A new session resumes from this file alone.

| Phase | What | Status |
|---|---|---|
| F0 | Foundation: design system, shell, auth | **reported** 2026-10-02 (the owner asked to go on to F1 without waiting) |
| F1 | Student: dashboard, courses, lesson | **reported** 2026-10-02 |
| F2 | Student: practice task, quiz, work list | **reported** 2026-10-02 |
| F3 | Student: timetable, grades, attendance, the rest | **reported** 2026-10-02 |
| F4a | Teacher: timetable, class session, journal | not started |
| F4b | Teacher: authoring, review, gradebook, analytics | not started |
| F5a | Admin: reference data, users, structure, audit | not started |
| F5b | Admin: timetable builder | not started |
| F6 | Polish, motion, accessibility, e2e, packaging | not started |

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

## F4a — Teacher: timetable, class session, journal (next)

Not started. The prompt is in `docs/05-FRONTEND-PROMPTS.md`.
