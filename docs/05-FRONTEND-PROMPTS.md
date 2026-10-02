# AIFU Learning Platform — Frontend build prompts

Ready-to-paste prompts for the agent that builds `frontend/`. One prompt = one phase = one
session. Give them in order; do not start a phase before the previous one is accepted.

| Phase | What | Spec IDs | Stop for the owner's review |
|---|---|---|---|
| F0 | Foundation: design system, shell, auth | G-01…G-09 | **Yes — the look is decided here** |
| F1 | Student: dashboard, courses, lesson | S-01, S-03, S-04 | Yes |
| F2 | Student: practice task, quiz, work list | S-05, S-04.4, S-06, S-07 | Yes |
| F3 | Student: timetable, grades, attendance, the rest | S-02, S-08, S-09, S-10 | |
| F4a | Teacher: timetable, class session, journal | T-01, T-02, T-03, T-10, T-11 | Yes |
| F4b | Teacher: authoring, review, gradebook, analytics | T-04…T-09 | |
| F5a | Admin: reference data, users, structure, audit | A-01, A-03…A-07 | |
| F5b | Admin: timetable builder | A-02 | Yes |
| F6 | Polish, motion, accessibility, e2e, packaging | all + §9 demo | Yes — acceptance |

## How to run

The phases below are not pasted one by one. The agent is started once and then driven with
"davom et" (continue); it follows the phase protocol of `AGENTS.md` §8 and keeps its place in
`frontend/PROGRESS.md`.

1. Start the backend: `docker compose up -d` in the repository root; wait until
   `http://localhost:8080/actuator/health` says `UP`.
2. Open the repository root (`aifu-platform`) in the agent's IDE and give it the start prompt:

```
Read AGENTS.md and follow it exactly, including §8 (phase protocol). Then start phase F0: open
docs/05-FRONTEND-PROMPTS.md, read the F0 prompt block and carry it out. From now on, whenever I
write "davom et", go on as AGENTS.md §8 says. Talk to me in Uzbek.
```

3. In F0 the agent stops once, after writing `frontend/DESIGN-DECISIONS.md`. Read it; "davom et"
   approves it.
4. When a phase is reported, open `http://localhost:5173`, sign in with the demo accounts
   (`docs/03-API-GUIDE.md` §9) and look at the screens on a desktop and a phone width.
5. Satisfied → "davom et" starts the next phase. Not satisfied → describe the problem in your own
   words (or use the helper prompts at the end of this file); the agent fixes it and stays in
   the same phase.
6. A new session at any time is fine: write "davom et" and the agent resumes from
   `frontend/PROGRESS.md`.

The phase blocks below are what the agent reads and carries out.

---

## F0 — Foundation and the look

```
You are building the frontend of the AIFU Learning Platform. Read AGENTS.md first and follow it
exactly. Then read, in this order: docs/01-PRODUCT-SPEC.md, docs/04-DESIGN-BRIEF.md,
docs/03-API-GUIDE.md (sections 1–4 and 9), docs/api/ENDPOINTS.md. Load the skills
aifu-design-system, aifu-api, aifu-screen-done, frontend-design, tailwind, ui-typography,
ui-surfaces, ui-interactions, type-safety.

This phase decides how the whole product looks. The owner rejected two earlier frontends for
looking generic. The bar: the platform of a major university (MIT OCW, Harvard, Stanford Online,
ETH, edX) — typography-led, restrained, exact, finished. Take the time to design; do not start
from the default shadcn look.

PHASE F0 — foundation. Spec: G-01 … G-09.

Step 1 — design, then wait for my approval. Create frontend/PROGRESS.md (AGENTS.md §8) and write
frontend/DESIGN-DECISIONS.md with: the concept
in five sentences; the font pairing (and the result of the glyph test from the brief §5); the
full token set (colour steps for light and dark with contrast ratios, six session-type colours,
verdict colours, type scale, spacing, radii, shadows, durations, easings); the app shell layout
at 390 / 768 / 1440; how the shield motif is used; the login page composition. Summarise it for
me, set the phase to "waiting for design approval" and stop.

Step 2 — after approval, build:
1. Scaffold frontend/ with the stack of AGENTS.md §4 and the structure of §5. Scripts: dev,
   build, preview, lint, typecheck, test, i18n:check, api:types, e2e. Vite proxies /api to
   http://localhost:8080. Strict TypeScript, ESLint with zero warnings allowed. Create
   frontend/.gitignore, frontend/BACKEND-REQUESTS.md (empty, with the entry format) and
   frontend/AGENTS.md — your own notes for the next sessions: commands, folder map, conventions
   you settled on, how to take screenshots. Keep it current at the end of every phase.
2. Typed API client from docs/api/openapi.json (skill aifu-api): generated schema, client, auth
   with single-flight refresh, error mapper, TanStack Query provider with sane defaults.
3. Design tokens and the shared/ui components listed in the brief §10, each with all its states
   in both themes, and the /dev/ui route (development only) that shows every one of them.
4. i18n: uz (default), ru, en; language switch that also saves PATCH /auth/me; the i18n:check
   script (same keys in all three files, none missing or unused).
5. Theme: light / dark / system, remembered, no flash on load.
6. Auth (G-08): login page (the public face — brief §9), forced password change when
   mustChangePassword is true, silent refresh, logout, logout everywhere, return to the deep
   link after login, session-expired handling.
7. App shell (brief §7) with role-aware navigation built from GET /auth/me (G-09), the role
   switch for accounts with several roles (try n.ayitbayev: teacher + admin), the command
   palette shell, the notification bell with the unread count, the profile menu.
8. Role home routes as real pages with a page header and an honest empty state ("this section
   is built in a later phase" is acceptable only here), plus the system pages: 404, no access,
   offline, error boundary with the request id.
9. Tests: unit tests for the error mapper, the refresh logic and the i18n check; Playwright
   projects "desktop" (1440×900) and "phone" (390×844) with flows: login as student3, as
   n.ayitbayev and as admin; wrong password; language switch; theme switch; a student opening an
   /admin URL sees "no access"; reload keeps the session.

Rules that matter most here: no mock data; no invented facts about the university; no text
outside t(); every component from tokens only.

Finish with the aifu-screen-done procedure for: login, change password, the shell in each role,
/dev/ui, and each system page — screenshots at 390 / 768 / 1440, light and dark, uz / ru / en.
Look at them and fix what is not at the level of the brief. Then give me the phase report and set the phase to "reported".
```

**Owner's check for F0:** the login page and `/dev/ui`. If they do not look like a serious
university's product, do not go on — use the design review prompt below until they do.

---

## F1 — Student: dashboard, courses, lesson

```
Read AGENTS.md and frontend/AGENTS.md, frontend/DESIGN-DECISIONS.md, then
docs/01-PRODUCT-SPEC.md §4 (S-01, S-03, S-04), docs/04-DESIGN-BRIEF.md §9,
docs/03-API-GUIDE.md §5 and §6 (Student). Load the skills aifu-design-system, aifu-api,
aifu-screen-done, ui-typography, ui-motion, css-animations, gsap-react.

PHASE F1 — the student's core. Demo account: student3 (group BK-DI26/1, course Dasturlash).

Plan first (spec IDs → endpoints → components → states → layouts at 390 / 768 / 1440), write
the plan into frontend/PROGRESS.md, then build without waiting:

1. S-01 Dashboard: today's classes (GET /timetable/today) with the current or next class marked;
   continue learning; the next five open deadlines; recent grades; unread notifications. Four
   blocks that load independently, each with its own skeleton, empty and error state.
2. S-03 Courses list and the course page with tabs Plan / Syllabus / Work / Grades, and the
   "Continue" action (continueLessonId from the plan).
3. S-04 Lesson page — the core of the product; make it read like a chapter of a fine textbook
   (brief §9). Build the block renderer for every block type of the API guide §5: TEXT, CALLOUT,
   CODE_EXAMPLE (consecutive code blocks become one tabbed example; the chosen language is
   remembered across the app), IMAGE / VIDEO / AUDIO / DIAGRAM / FILE (files fetched as blobs
   with the token), TABLE, CASE_STUDY, LINK, EMBED, QUIZ_REF, CODING_TASK_REF, ACTIVITY_REF.
   Table of contents from the h2 / h3 headings with scroll-spy; reading progress; previous /
   next; practice links with their status; "Mark as done" (POST /lessons/{id}/progress); the
   locked state for a lesson whose prerequisites are missing; the fallback-language mark;
   language switch without losing the reading position (S-04.6).
   S-04.4 "Try it" is built in the next phase: leave the button out for now, not disabled.
4. One <RichText> component (DOMPurify) for all server HTML, styled as the lesson typography.

The lessons in the demo database are short and in Uzbek only; richer three-language content is
being written separately. Design for long lessons: test your layout with the longest lesson you
can find and make sure a ten-screen chapter with many code blocks and tables stays comfortable.
Do not add sample text to make a lesson look full.

Verify with aifu-screen-done (dashboard, courses, course page with each tab, lesson page; also
student1 for a different course and an account with an empty state). Add Playwright flows:
dashboard → continue → lesson → mark as done → the plan shows it done. Phase report at the end.
```

---

## F2 — Student: practice task, quiz, work list

```
Read AGENTS.md, frontend/AGENTS.md, frontend/DESIGN-DECISIONS.md, then docs/01-PRODUCT-SPEC.md
(S-05, S-04.4, S-06, S-07), docs/04-DESIGN-BRIEF.md §8–§9, docs/03-API-GUIDE.md §4, §6 and §7.
Load the skills aifu-design-system, aifu-api, aifu-screen-done, gsap-react, gsap-timelines,
ui-motion, ui-interactions.

PHASE F2 — the code editor and the quiz. Demo account: student3. The task "Kabisa yili"
(DAS-S1-A3) has a hint, a deadline and earlier submissions; "Tub sonni aniqlash" has three hints;
every task has starter code in Python, Java and C++.

Plan first, write the plan into frontend/PROGRESS.md, then build without waiting:

1. shared/editor: a CodeMirror 6 wrapper — python / java / cpp, syntax highlighting, auto-indent,
   bracket matching, line numbers, tab = 4 spaces, font size control, themes matching the app's
   light and dark, full screen, read-only mode (used for code in lessons and in history).
   Lazy-loaded.
2. S-05 Practice task page — an IDE, not a form (brief §9):
   - statement, input / output format, constraints, examples with copy, hints revealed one at a
     time (GET /coding-tasks/{id}/hints, POST …/reveal);
   - language selector Python / Java / C++ with the task's starter code; the draft is kept per
     task and per language in localStorage and survives reload and language switch; "Reset to
     starter" asks first;
   - Run with custom input (POST /runs) → stdout, stderr, compile output, time, memory;
   - Run examples (mode EXAMPLES) → pass / fail per example with a diff of expected vs actual;
   - Submit (POST /submissions with an Idempotency-Key) → live status queued → running → the
     verdict reveal: tests tick in one by one, then the verdict, tests passed, score. This is the
     best moment of the product: choreograph it with GSAP, calm for failures, a restrained
     celebration for Accepted, instant under prefers-reduced-motion;
   - history (GET /coding-tasks/{id}/submissions/me): attempts with time, language, verdict,
     score; open one read-only; restore it into the editor; attempt limit and deadline shown;
   - teacher feedback on a submission (GET /feedback/me?targetType=SUBMISSION&targetId=);
   - shortcuts Ctrl+Enter (run) and Ctrl+Shift+Enter (submit), shown on the buttons;
   - resizable panes on desktop; tabs Task / Code / Result with pinned Run and Submit on a phone;
   - every failure state: compilation error with the compiler output, runtime error, time limit,
     rate limit (429), attempt limit (422), the platform failing to run the code (status FAILED).
3. S-04.4 "Try it" on lesson code examples: a small runner opening in place (POST /runs without
   codingTaskId).
4. S-06 Quiz: start screen (rules, time limit, attempts left, deadline), one question per screen
   with the navigator, answers saved as the student goes, a countdown that auto-submits, resume
   after reload, every question type the API has, submit confirmation, the result and the review
   when allowed.
5. S-07 Assignments: one list across courses (GET /activities/me) with filters by course, type
   and state, sorted by deadline; each row opens its quiz or task.

Really run code while verifying: a correct solution in each of Python, Java and C++; a wrong
answer; a compile error; an infinite loop (time limit). Verify with aifu-screen-done and add
Playwright flows for acceptance steps 4 and 5 of the spec §9 (solve in Python, Java and C++;
take the quiz). Phase report at the end.
```

---

## F3 — Student: timetable, grades, attendance, the rest

```
Read AGENTS.md, frontend/AGENTS.md, frontend/DESIGN-DECISIONS.md, then docs/01-PRODUCT-SPEC.md
(S-02, S-08, S-09, S-10), docs/04-DESIGN-BRIEF.md §9, docs/03-API-GUIDE.md §4 and §6.
Load the skills aifu-design-system, aifu-api, aifu-screen-done, ui-motion, css-animations.

PHASE F3 — the rest of the student's side.

Plan first, write the plan into frontend/PROGRESS.md, then build without waiting:

1. S-02 My timetable — people open it every day; make it beautiful and exact (brief §9).
   Build the week grid as a shared feature component: the teacher's timetable and the admin's
   builder reuse it. Week switch, dates, the toq / juft (odd / even) chip from the server, today
   and the current pair marked, session-type colours with a legend, the class sheet with the
   linked lesson (GET /class-sessions/{entryId}/{date}/topic), holidays, cancelled classes and
   moved classes (old slot struck through with the reason, new slot marked). Phone: day strip +
   day list. Never compute parity or week numbers in the browser.
   Check with student3 in the week of 19 October 2026 (a moved lecture), 16 October (a cancelled
   practical), 1 October (a holiday), and with xolmatov.u (odd / even subgroups A and B).
2. S-08 Grades: per course the JN / ON / YaN components with weights, earned / maximum, pass
   gates, admission to the final exam, final score and grade, and the history of changes.
3. S-10 My attendance: per course — held, attended, missed with dates, percentage.
4. S-09 Notifications (list, unread, mark read, read all, links from payloadJson), global
   search in the command palette (GET /search), profile (name, group, language, theme, change
   password), announcements (GET /announcements/me and on the course page).
5. Finish the student dashboard with anything these screens add.

Verify with aifu-screen-done. Playwright: the timetable weeks named above on desktop and phone;
grades; notifications. Phase report at the end.
```

---

## F4a — Teacher: timetable, class session, journal

```
Read AGENTS.md, frontend/AGENTS.md, frontend/DESIGN-DECISIONS.md, then docs/01-PRODUCT-SPEC.md
§5 (T-01, T-02, T-03, T-10, T-11), docs/04-DESIGN-BRIEF.md §9, docs/03-API-GUIDE.md §5–§6.
Load the skills aifu-design-system, aifu-api, aifu-screen-done, ui-interactions, ui-motion.

PHASE F4a — the teacher in the classroom. Demo account: n.ayitbayev (five practical classes a
week; Dasturlash on Fridays has attendance history and a lesson plan on the first practical).

Plan first, write the plan into frontend/PROGRESS.md, then build without waiting:

1. T-01 Teacher dashboard: today's classes with "start class", submissions waiting for review,
   at-risk students, deadlines closing this week, quick links to the courses.
2. T-02 My weekly timetable (reuse the week grid): groups and rooms on the cards, total hours of
   the week, odd / even, print stylesheet for a clean A4 landscape page.
3. T-03 Class session — used live, standing at the desk (brief §9): header; the topic with the
   linked or suggested lesson (PUT to change it); attendance with present / absent / late /
   excused, "all present" in one tap, optimistic save, editable later; notes that save as typed;
   the teacher-only lesson plan (LESSON_PLAN blocks of GET /teacher/lessons/{id}).
4. T-03.3 Presentation mode: a separate full-screen design for a projector — very large type,
   no chrome, arrow keys and on-screen arrows between sections, code readable from the back row.
5. T-04.3 Calendar-thematic plan: GET /teacher/courses/{id}/class-plan as a table of classes by
   date with their topics, editable row by row.
6. T-10 Groups and the attendance journal: the roster; the grid students × class dates with
   totals and percentages, sticky headers, filter by group and period, export to Excel.
7. T-11 Announcements: post to a course or a group, list, delete.

Verify with aifu-screen-done. Playwright: acceptance step 2 of the spec §9 (see the week, open
today's class, mark attendance, open the lesson in presentation mode) on desktop and phone.
Phase report at the end.
```

---

## F4b — Teacher: authoring, review, gradebook, analytics

```
Read AGENTS.md, frontend/AGENTS.md, frontend/DESIGN-DECISIONS.md, then docs/01-PRODUCT-SPEC.md
§5 (T-04 … T-09), docs/04-DESIGN-BRIEF.md §9, docs/03-API-GUIDE.md §5–§6, and the teacher
sections of docs/api/ENDPOINTS.md. Load the skills aifu-design-system, aifu-api,
aifu-screen-done, compound-components, type-safety.

PHASE F4b — the teacher's tools. Demo accounts: n.ayitbayev, b.akbarov.

Plan first, write the plan into frontend/PROGRESS.md, then build without waiting:

1. T-04 Curriculum tree: modules → lessons; create, rename, reorder by drag (and by keyboard),
   publish / unpublish, soft delete; the backend's hour-budget warnings shown.
2. T-04.2 Lesson editor: add / edit / reorder / delete blocks of every type, including the
   teacher-only LESSON_PLAN; language tabs UZ / RU / EN per block with a marker for a missing
   translation; file and image upload (POST /files); live preview through the same renderer the
   student sees; autosave state always visible; 409 CONCURRENT_MODIFICATION handled.
3. T-05 Coding task editor: statement in three languages; starter code and reference solution
   for each of Python, Java, C++ in the code editor; test cases (visible / hidden, weight);
   limits; hints; "Verify solutions" (POST then poll GET /teacher/coding-tasks/{id}/verification)
   showing the result per language and per test before publishing.
4. T-06 Quiz editor: settings, questions of every type, options and answer keys, translations,
   reorder, publish with the server's validation errors shown where they belong.
5. T-07 Submissions review: the queue per task with filters; a submission with its code
   (read-only editor), test results, and the feedback form.
6. T-08 Gradebook: students × assessment components; record and void a grade with a reason
   (grades are append-only — show the history); server-computed final score, admission and
   grade; filter by group; keyboard navigation between cells; export to Excel.
7. T-09 Analytics: completion funnel, lesson engagement, quiz item analysis, coding-task
   statistics, at-risk students — charts in the design system's tokens, each with a text summary
   and an accessible table alternative.

Verify with aifu-screen-done. Playwright: acceptance step 6 of the spec §9 (review a submission,
leave feedback, record a grade; the student sees both). Phase report at the end.
```

---

## F5a — Admin: reference data, users, academic structure, audit

```
Read AGENTS.md, frontend/AGENTS.md, frontend/DESIGN-DECISIONS.md, then docs/01-PRODUCT-SPEC.md
§6 (A-01, A-03 … A-07), docs/04-DESIGN-BRIEF.md §9, docs/03-API-GUIDE.md §6 (Administrator),
and the admin sections of docs/api/ENDPOINTS.md. Load the skills aifu-design-system, aifu-api,
aifu-screen-done, compound-components, type-safety.

PHASE F5a — the administrator's records. Demo account: admin.

Plan first, write the plan into frontend/PROGRESS.md, then build without waiting — one consistent
list → detail → form pattern, applied to:

1. A-03 Reference data: groups (roster, archive), rooms, the bell schedule (pair slots),
   holidays, first-week parity of a term.
2. A-05 Users: search and filters, create and edit, roles, status, reset password, unlock;
   import students from an Excel / CSV file read in the browser — POST /admin/users/import with
   dryRun: true first, show the outcome row by row, then the real import.
3. A-06 Academic structure: academic years and terms; subjects (translations, prerequisites,
   outcomes); courses (teachers, session-hour allocations, assessment components, translations,
   status); enroll one student, many, or a whole group.
4. A-04 Teaching load: lines per term, by hand or from the department's Excel sheet read in the
   browser; view per teacher.
5. A-07 Audit log with filters; roles and permissions catalogue.
6. A-01 Admin dashboard: counts, today's classes, unscheduled load and conflicts needing
   attention, recent audit events.

Tables keep page, sort and filters in the URL. Destructive actions ask for confirmation and name
what will be affected. Verify with aifu-screen-done; Playwright: create a group, a room and a
holiday; import three students with a dry run. Phase report at the end.
```

---

## F5b — Admin: timetable builder

```
Read AGENTS.md, frontend/AGENTS.md, frontend/DESIGN-DECISIONS.md, then docs/01-PRODUCT-SPEC.md
A-02 (every line), docs/04-DESIGN-BRIEF.md §8–§9, docs/03-API-GUIDE.md §4 and §6, and
docs/02-ARCHITECTURE.md §4.2 (the conflict rule). Load the skills aifu-design-system, aifu-api,
aifu-screen-done, ui-interactions, ui-motion, gsap-react.

PHASE F5b — the timetable builder, the most complex screen of the product and the owner's
explicit request. Demo account: admin; the term 2026/2027-1 has a published timetable.

Plan first — this screen deserves a careful one: the state model (selected timetable, view,
filter, parity toggle, drag state, pending check), the keyboard model, the layout at 1440 and at
390 — write it into frontend/PROGRESS.md, then build without waiting:

1. A-02.1 Term and timetable selection; DRAFT / PUBLISHED / ARCHIVED unmistakable; create a
   draft, copy from another timetable (copyFromId), rename, publish, archive.
2. A-02.2 Three views of one grid: by group, by teacher, by room; days Mon–Sat × pairs; a toggle
   for odd / even / every week (reuse the week grid).
3. A-02.3 Add and edit a class in a side panel: course, session type, teacher (from the
   course's teachers), one or more groups, room, day, pair, parity, optional dates, optional
   subgroup.
4. A-02.4 Conflicts: a 409 TIMETABLE_CONFLICT is not a failure. Show details.conflicts (hard —
   name the clashing class, saving is blocked) and details.warnings (soft — can be overridden by
   repeating with force: true and a typed reason).
5. A-02.5 Drag and drop with dnd-kit, also by keyboard: while dragging, call
   POST …/entries/check (debounced) to mark free and conflicting slots; a conflict shakes the
   card once and explains; undo the last change.
6. A-02.6 The Load panel: planned versus scheduled hours per subject and session type for the
   selected group or teacher, what is still unscheduled.
7. A-02.7 One-off changes for a date: cancel, move, replace the teacher or room, with a reason;
   list and delete them; preview a concrete week (GET /admin/timetables/{id}/week).
8. A-02.8 Print and export by group, teacher or room (print stylesheet; Excel).

Work on a draft copy while testing; do not leave the demo timetable broken. Verify with
aifu-screen-done; Playwright: acceptance step 1 of the spec §9 (build a week for two groups, get
a conflict when placing a teacher twice, publish). Phase report at the end.
```

---

## F6 — Polish, motion, accessibility, acceptance

```
Read AGENTS.md, frontend/AGENTS.md, frontend/DESIGN-DECISIONS.md, docs/04-DESIGN-BRIEF.md (all),
docs/01-PRODUCT-SPEC.md §3 and §9. Load every project skill, plus gsap-react, gsap-timelines,
gsap-performance, gsap-text, css-animations, ui-motion, and web-design-guidelines.

PHASE F6 — make it excellent, then prove it.

1. Design pass. Screenshot every screen (390 / 768 / 1440, light and dark, uz / ru / en) and
   review each against the brief as a strict design critic. List the weaknesses per screen in
   frontend/DESIGN-REVIEW.md, ranked, and fix them: inconsistent spacing, weak hierarchy,
   default-looking controls, crowded phone layouts, dark-theme contrast, long Russian strings,
   empty and error states that were left plain.
2. Motion pass: the eight moments of the brief §8, built with the duration and easing tokens;
   nothing else moves. 60 fps on a mid-range phone; all of it off under prefers-reduced-motion.
3. Accessibility: keyboard through every flow, focus order and focus return, labels, contrast,
   reduced motion; run axe on every route in Playwright and fix every violation.
4. Performance: route-level code splitting checked; the editor, charts, Excel and GSAP out of
   the initial bundle; fonts subset and preloaded; no layout shift; report the bundle sizes and
   the Lighthouse numbers for the login, the dashboard and the lesson page.
5. Resilience: API down, slow network, expired session in the middle of a form, two tabs, a
   submission in flight during a reload — each handled.
6. The acceptance demo of the spec §9 — all six steps — as Playwright flows on the desktop and
   the phone project, in uz, ru and en, against the real backend.
7. Packaging: frontend/Dockerfile (multi-stage build → nginx) and frontend/nginx.conf (SPA
   fallback, /api proxied to the api service, long cache for hashed assets, security headers).
   Do not edit docker-compose.yml: write the service block you propose into
   frontend/BACKEND-REQUESTS.md.
8. frontend/README.md: how to run, test and build; the structure; the decisions.

Final report: every spec ID with its state (done and verified / done, not verified / not done),
the open entries of BACKEND-REQUESTS.md, and what you would improve next.
```

---

## Helper prompts

**Design review** (when a screen works but does not look good enough):

```
Load aifu-design-system, frontend-design and aifu-screen-done. Take fresh screenshots of
<screen> at 390 and 1440, light and dark, in ru. Look at them as a senior product designer
comparing against docs/04-DESIGN-BRIEF.md and the level of MIT OCW / Stanford Online / edX. Name
the ten weakest points, most visible first (hierarchy, spacing, typography, colour use, density,
states, phone layout). Propose the fix for each, wait for my go, then fix them and show the new
screenshots next to the old ones. Do not change behaviour.
```

**Fix round** (after the owner's check):

```
Read AGENTS.md and frontend/AGENTS.md. I checked phase <id> and found these problems:
1. <screen, account, what I did, what I saw, what I expected>
2. …
For each: reproduce it first, find the cause, fix it, and verify in the browser (aifu-screen-done).
Do not touch anything unrelated. Report per item: cause, fix, how you verified.
```

**The API lacks something:**

```
Do not invent data and do not work around it. Add the entry to frontend/BACKEND-REQUESTS.md in
the format of the aifu-api skill, build the rest of the screen with an honest empty state for the
missing part, and tell me in the report.
```
