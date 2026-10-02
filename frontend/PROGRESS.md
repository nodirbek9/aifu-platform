# Frontend build — progress

The single record of progress (AGENTS.md §8). A new session resumes from this file alone.

| Phase | What | Status |
|---|---|---|
| F0 | Foundation: design system, shell, auth | **reported** 2026-10-02 (the owner asked to go on to F1 without waiting) |
| F1 | Student: dashboard, courses, lesson | **reported** 2026-10-02 |
| F2 | Student: practice task, quiz, work list | **in progress** — the task, "Try it" and the work list are done; the quiz (S-06) is next |
| F3 | Student: timetable, grades, attendance, the rest | not started |
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

## F2 — Student: practice task, quiz, work list (in progress)

Done and verified:
- `shared/editor`: CodeMirror 6 for Python, Java and C++ — highlighting from the design tokens
  (follows light / dark by itself), auto-indent, bracket matching, line numbers, four-space tab,
  font size, read-only mode. A separate chunk (607 kB), loaded only where code is shown.
- S-05 practice task (`/student/courses/:courseId/tasks/:taskId`, a focus page):
  statement, input / output, constraints, limits, examples with copy, hints opened one at a
  time (S-05.1); language tabs with each language's starter code, a draft per task and language
  that survives reload and language switch, reset with confirmation (S-05.2); font size, full
  screen, Ctrl+Enter / Ctrl+Shift+Enter shown on the buttons (S-05.3); Run with custom input
  (S-05.4); Run examples with input / expected / actual for a failed one (S-05.5); Submit with
  an idempotency key, live queued → running status, the verdict reveal (tests tick in, then the
  verdict and the score), hidden tests shown without data (S-05.6); history, opening an attempt
  read-only and restoring it into the editor, attempts left and the deadline (S-05.7); the
  teacher's feedback under a submission (S-05.8). Resizable panes on desktop; Task / Code /
  Result tabs with pinned actions on a phone.
- S-04.4 "Try it": a lesson example opens in a runner in place; lesson code is now highlighted.
- S-07 Assignments: one list across courses, filters by course, type and state kept in the
  address, sorted by deadline.
- Gates: lint, typecheck, unit tests, i18n (341 keys × 3), build. `npm run e2e`: 46 passed
  (23 flows × desktop and phone), with code really running in the sandbox: a correct Python
  solution run, checked on the examples, submitted and accepted; a wrong answer; a Java
  compilation error; a hint; "Try it" in a lesson; the work list filters.
- Screenshots of the task page (light, dark, phone tabs), the runner and the work list — looked at.

Not done yet:
- **S-06 Quiz** — the whole screen (start, questions with navigator, timer, autosave, resume,
  result and review). The route `/student/courses/:courseId/quizzes/:quizId` is still a
  placeholder. This is what remains of F2.
- A solution in Java and in C++ submitted through the UI (acceptance step 4): only Python is
  submitted by the flow; Java is exercised by a run (compile error). The backend proved all
  three end to end; the UI path is the same code.
- The 429 (rate limit) and 422 (attempt limit) messages: built, not provoked in a flow.
- The verdict reveal was seen running in the flow, not reviewed frame by frame.

Demo data: the e2e global setup also clears student3's submissions to DAS-S1-A8 before a run.
