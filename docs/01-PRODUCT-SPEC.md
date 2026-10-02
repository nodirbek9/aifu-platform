# AIFU Learning Platform — Product Spec (v3)

Status: draft for the owner's review · Date: 2026-10-02 · Owner: N. Ayitbayev (AIFU)

This is the **source of truth for what the product does**, written screen by screen. Earlier
attempts had strong engineering documents and no product spec, so each model built its own idea of
the product. Every feature below has an ID and acceptance criteria; a feature is done only when
every criterion can be shown on a running system.

Legend: ✅ backend exists · 🔧 backend exists, needs a small addition · 🆕 new backend work.
🤖 marks a detail the spec author decided because the owner didn't specify it — change freely.

## 1. Product in one paragraph

A learning platform for AIFU (Aniq va Ijtimoiy Fanlar Universiteti). Students open a subject, read
each topic as a **complete lesson** (theory, examples, runnable code, summary, self-check), solve
**practice tasks in a real code editor in Python, Java or C++** that are checked automatically, take
quizzes, and see their grades by the university's JN / ON / YaN scheme. Teachers see **their weekly
timetable**, teach from the lesson pages, edit content, review submissions and keep the gradebook.
The administrator (o'quv bo'limi) **builds the timetable** for groups, teachers and rooms, and
manages users, groups, subjects and the academic calendar. Interface and lesson content are in
**Uzbek, Russian and English**.

## 2. Users and roles

| Role | Who | Main jobs |
|---|---|---|
| STUDENT | ~200 students in groups such as BK-DI26/1 | Learn topics, solve tasks, take quizzes, see timetable and grades |
| TEACHER | ~15 teachers | Weekly timetable, teach, author content, review, grade, attendance |
| ADMIN | O'quv bo'limi / system admin | Timetable, groups, rooms, users, subjects, courses, calendar, audit |

One account may hold several roles (a teacher who is also admin); the UI offers a role switch.

## 3. Global requirements (apply to every screen)

- G-01 **Three languages.** UZ / RU / EN switcher always visible. Every UI string is translated.
  Lesson content is shown in the chosen language; if a translation is missing, show the fallback
  language with a small "shown in Uzbek" note (the API returns `resolvedLocale` and `fallback`).
- G-02 **Responsive.** Fully usable at 390 px (phone), 768 px (tablet) and ≥1280 px (desktop). No
  horizontal page scroll. Tables become stacked rows or scroll inside their own container.
- G-03 **Four states everywhere:** loading skeleton, empty state with a next action, error state
  with retry, content. No blank screens, no infinite spinners.
- G-04 **No mock data.** Everything comes from the API. A missing endpoint is a backend task, not a
  reason to fake data.
- G-05 **Light and dark theme**, remembered per user; respects the system setting on first visit.
- G-06 **Motion with purpose** (see ARCHITECTURE §7): page and list transitions, progress
  animations, verdict reveal. Smooth at 60 fps, off under `prefers-reduced-motion`.
- G-07 **Accessibility:** keyboard reachable, visible focus, labels on inputs, contrast ≥ 4.5:1.
- G-08 **Auth:** login with username + password; forced password change on first login; silent
  token refresh; logout everywhere. A deep link opened while logged out returns there after login.
- G-09 **Role-aware navigation:** each role sees only its own sections; a URL of another role's
  page shows a clear "no access" page, never a crash.

## 4. Student

### S-01 Dashboard (home) 🔧
What the student sees first.
- S-01.1 **Today's classes** from the timetable: time, subject, session type (lecture / practical /
  lab / seminar), room, teacher; the current or next class is highlighted. Empty day → "no classes
  today" + next class date.
- S-01.2 **Continue learning**: the lesson the student was last in, with course progress.
- S-01.3 **Deadlines**: the next 5 open quizzes and tasks with due dates; overdue in red.
- S-01.4 **Recent grades** (last 5 published) and unread notifications count.
- S-01.5 Greeting with name and group; all four blocks load independently.

### S-02 My timetable 🆕
- S-02.1 Week grid: days Mon–Sat × pair slots (1-para 08:30 … 🤖), each cell shows subject, type
  (colour-coded), room, teacher. Today's column and the current pair are highlighted.
- S-02.2 Switch week (previous / next / "this week"); the header shows the dates and whether it is
  a **toq (odd) or juft (even)** week; entries valid only in one parity appear only in that week.
- S-02.3 On a phone: a day list with a day switcher instead of the grid.
- S-02.4 Tapping a class opens a sheet: subject, teacher, room, linked lesson topic (if the teacher
  linked one) with a "open lesson" button.
- S-02.5 Holidays and cancelled classes are marked; a moved class shows its new slot.

### S-03 My courses and course page ✅
- S-03.1 Courses list: cards with subject name, code, teacher, semester, progress %, next lesson.
- S-03.2 Course page tabs: **Plan** (modules → lessons, each with type badge, hours, status:
  not started / in progress / done, locked if prerequisites are missing), **Syllabus** (learning
  outcomes, hour budget, assessment scheme JN/ON/YaN with weights and pass gates), **Work**
  (all quizzes and tasks of the course with status and score), **Grades**.
- S-03.3 A visible "Continue" action goes to the next unfinished lesson.

### S-04 Lesson page — the core of the product 🔧
The owner's main complaint was empty lessons. A lesson is a complete, readable chapter.
- S-04.1 Layout: content column (max ~70 characters wide, comfortable type), a sticky **table of
  contents** built from the lesson's headings (desktop: right rail; phone: a drawer), a reading
  progress bar, previous / next lesson.
- S-04.2 Every lesson shows, in this order (content rules in `05-CONTENT-PROMPTS.md`): title and
  meta (type, hours, reading time) → **learning objectives** → **theory** in sections with headings
  → **worked examples** → **code examples** with syntax highlighting, copy button and language tabs
  (Python / Java / C++) → diagrams and tables where they help → **key terms** → **summary** →
  **self-check questions** (click to reveal the answer) → **practice**: links to the lesson's
  coding tasks and quiz, with their status.
- S-04.3 Block types rendered: text, image, video, audio, code example, callout (note / tip /
  warning), diagram, table, case study, file download, link, embed, quiz reference, coding-task
  reference, activity reference.
- S-04.4 **Try it**: a code example can be opened in a small runner (same editor as S-05) and
  executed without leaving the lesson. 🆕 needs the run endpoint.
- S-04.5 "Mark as done" (or automatic when the student reaches the end and passes the self-check
  🤖); status is saved on the server and shown in the course plan. 🔧 needs the per-lesson status
  list endpoint.
- S-04.6 Language switch re-renders the lesson in the chosen language without losing position.

### S-05 Practice task (code editor) 🔧
The owner's second main complaint: no place to write code, and not in three languages.
- S-05.1 Split view. Left: statement (description, input and output format, constraints,
  examples with copy buttons, hints the student can reveal one by one). Right: editor. On a phone:
  tabs "Task / Code / Result".
- S-05.2 **Language selector: Python, Java, C++.** Each language has its own starter code. The
  student's draft is kept **per task and per language** (survives reload and language switch).
  "Reset to starter" asks for confirmation.
- S-05.3 Editor: syntax highlighting, auto-indent, bracket matching, line numbers, tab = 4 spaces,
  font size control, light/dark, full-screen, shortcuts (Ctrl+Enter run, Ctrl+Shift+Enter submit).
- S-05.4 **Run** with custom input: shows stdout, stderr, compile errors, time and memory. Not
  graded, does not count as an attempt. 🆕
- S-05.5 **Run examples**: runs the visible tests and shows pass/fail per example with a diff of
  expected vs actual output. 🆕 (same endpoint)
- S-05.6 **Submit**: checked against hidden tests; while queued/running the page shows a live
  status; then the verdict (Accepted, Partial, Wrong answer, Compilation error, Runtime error, Time
  limit, Memory limit), tests passed (e.g. 7/10), score, and details of the visible tests only.
  Hidden test data is never shown. ✅
- S-05.7 Submission history: every attempt with time, language, verdict, score; clicking one shows
  its code (read-only) and can restore it into the editor. Attempt limit and deadline are shown.
- S-05.8 Teacher feedback on a submission is visible to the student. 🆕 student feedback endpoint

### S-06 Quiz ✅
- S-06.1 Start screen: rules, time limit, attempts left, deadline. Start creates an attempt.
- S-06.2 One question per screen with a question navigator; answers are saved as the student
  goes; a timer counts down and auto-submits at zero; leaving and returning resumes the attempt.
- S-06.3 Question types: single choice, multiple choice, true/false, short answer (and the other
  types the backend has).
- S-06.4 Result: score, pass/fail, and review of answers when the teacher allows it.

### S-07 Assignments (all my work) ✅
One list across all courses: quizzes, coding tasks, other activities; filters (course, type,
status: to do / submitted / graded / overdue); sorted by deadline.

### S-08 Grades ✅
Per course: JN / ON / YaN components with max points, earned points, pass gates, admission to the
final exam, final score and grade. History of grade changes is visible (grades are append-only).

### S-09 Notifications, search, profile ✅
Notification list with unread badge; global search over subjects, courses and lessons; profile
(name, group, language, theme, change password).

### S-10 My attendance 🆕
Per course: classes held, attended, missed (with dates), percentage.

## 5. Teacher

### T-01 Dashboard 🔧
Today's classes (from the timetable) with a "start class" action; submissions waiting for review;
at-risk students; deadlines closing this week; quick links to the courses.

### T-02 My weekly timetable 🆕 — the owner's explicit request
- T-02.1 Week grid of the teacher's own classes: time, subject, type, **group(s)**, room. Odd/even
  week switch like S-02. Total hours this week shown.
- T-02.2 Clicking a class opens the **class session** (T-03).
- T-02.3 Print / export the week as PDF; phone layout is a day list.

### T-03 Class session 🆕
The page a teacher opens during a class.
- T-03.1 Header: subject, group(s), room, time, date. **Topic of this class**: the teacher links
  one lesson from the course plan (suggested automatically: the next lesson of that session type).
- T-03.2 **Attendance**: the group's student list with present / absent / late / excused; "all
  present" in one tap; saved per class date; editable later.
- T-03.3 "Open lesson" shows the lesson in **presentation mode** (large type, no navigation
  chrome, arrow keys move between sections) for the projector.
- T-03.4 **Lesson plan** for the teacher (not visible to students): objectives, timing plan
  (e.g. 10 min revision, 40 min new topic, 25 min practice, 5 min summary), materials, questions to
  ask, homework. Stored as teacher-only blocks of the lesson. 🔧
- T-03.5 Notes for this class date (free text).

### T-04 Course management and lesson editor ✅
- T-04.1 Curriculum tree: modules → lessons; create, rename, reorder (drag), publish/unpublish,
  delete (soft). Hour-budget warnings from the backend are shown.
- T-04.2 Lesson editor: add / edit / reorder / delete blocks of every type; **language tabs
  UZ / RU / EN** per block with a marker for missing translations; live preview as the student
  sees it; file and image upload.
- T-04.3 Calendar-thematic plan (kalendar-tematik reja): table of weeks × lessons for the term
  showing which topic is planned for which class, generated from the timetable and editable. 🆕

### T-05 Coding task editor ✅ + 🔧
Statement in three languages; for **each of Python, Java, C++**: starter code and reference
solution; test cases (visible / hidden, weight); limits; hints. "Verify solutions" runs every
reference solution against the tests and shows the result before publishing. 🆕

### T-06 Quiz editor ✅
Quiz settings (time limit, attempts, shuffle, deadline, review policy), questions of every type,
options and answer keys, translations, reorder, publish.

### T-07 Submissions review ✅ + 🔧
Queue of submissions per task with filters (group, verdict, needs review); a submission opens
with code, test results and a feedback form (comment, manual score override where allowed).

### T-08 Gradebook ✅
Students × assessment components grid for a course; record / void a grade (append-only, with
reason); final score, admission and grade are computed by the server; filter by group; export
to Excel 🆕.

### T-09 Analytics ✅
Per course: completion funnel, lesson engagement, quiz item analysis, coding-task statistics,
at-risk students list.

### T-10 Groups and attendance journal 🆕
Group roster; attendance journal per course (students × class dates grid), totals and percent,
export to Excel.

### T-11 Announcements 🆕
Post a message to a course or a group; students get a notification.

## 6. Administrator

### A-01 Dashboard 🔧
Counts (students, teachers, groups, active courses); today's classes; **timetable conflicts and
unscheduled load** needing attention; recent audit events.

### A-02 Timetable builder 🆕 — the owner's explicit request
- A-02.1 Choose academic year and term. A timetable has a status: DRAFT → PUBLISHED. Students and
  teachers see only the published one; the admin can edit a draft copy and publish changes.
- A-02.2 Three views of the same data: **by group, by teacher, by room**. Grid: days (Mon–Sat) ×
  pair slots; a toggle for odd / even / every week.
- A-02.3 **Add a class**: course (subject delivery), session type, teacher (from the course's
  teachers), one or more groups (a lecture can be for a whole potok), room, day, pair, week parity,
  optional date range, optional subgroup (for labs that split a group).
- A-02.4 **Conflict check** on every add / move, before saving: the teacher, any of the groups, or
  the room is already busy in that slot and parity; the room capacity is smaller than the group
  size; the room type does not fit (a lab needs a computer room). The message names the clashing
  class. Hard conflicts block saving; warnings can be overridden with a reason.
- A-02.5 **Drag and drop** a class to another slot or day; the same checks run; undo last change.
- A-02.6 Side panel "**Load**": for the selected group or teacher, planned hours (from the teaching
  load / course allocations) vs scheduled hours per subject and session type, so the admin sees
  what is still unscheduled.
- A-02.7 One-off changes for a date: cancel a class, move it, replace the teacher or room;
  affected students and the teacher get a notification.
- A-02.8 Copy a timetable from a previous term as a starting draft. Print / export by group,
  teacher or room (PDF and Excel).

### A-03 Reference data 🆕
Groups (code like BK-DI26/1, programme, course year, size, curator 🤖); rooms (building, number,
capacity, type: lecture hall / classroom / computer lab); **bell schedule** (pair slots with start
and end times 🤖 default 80-minute pairs); holidays and non-teaching days.

### A-04 Teaching load (yuklama) 🆕
Import the department's Excel load sheet (subject, programme, course year, group, lecture /
practical / seminar / lab hours, teacher) or enter it by hand; view per teacher; this feeds the
"Load" panel of A-02.6.

### A-05 Users ✅ + 🔧
Create and edit users, roles, status (active / blocked), reset password, unlock; **import students
from CSV/Excel with their group**; assign students to groups in bulk.

### A-06 Academic structure ✅
Academic years and terms; subjects (translations, prerequisites, learning outcomes); course
deliveries (subject + term + cohort), teachers of a course, session-hour allocations, assessment
components; **enroll a whole group into a course** in one action 🔧.

### A-07 Audit log and system ✅
Audit browser with filters; roles and permissions catalogue.

## 7. Ideas beyond the brief (owner: tick what you want)

Not in scope until ticked. Each is small on its own.

- [ ] X-01 **Leaderboard and streaks** per course (opt-in), badges for solved tasks.
- [ ] X-02 **AI tutor** on lesson and task pages: explains an error or a concept, never gives the
      full solution; teacher can switch it off per task.
- [ ] X-03 **Plagiarism check** between students' submissions of one task (similarity report).
- [ ] X-04 **Exam mode** for quizzes and tasks: full-screen, tab-switch log, fixed time window.
- [ ] X-05 **Code playground**: a free editor (Python / Java / C++) not tied to a task.
- [ ] X-06 **Discussion** under each lesson (questions and answers).
- [ ] X-07 **Telegram notifications** (deadlines, grades, timetable changes).
- [ ] X-08 **Certificates / transcript PDF** per student.
- [ ] X-09 **Parent / curator view** of a student's progress (read-only).
- [ ] X-10 **Question bank** shared between courses, with random quiz generation.
- [ ] X-11 **Mobile app shell (PWA)**: install on the phone, offline reading of lessons.
- [ ] X-12 **Room booking** for extra classes outside the timetable.

## 8. Out of scope (v3)

Payments, proctoring by camera, SSO with university systems, group (team) submissions, video
conferencing, native mobile apps, automatic timetable generation.

## 9. Acceptance demo (how the owner checks the result)

Seed accounts for each role. The build is accepted when this walk-through works on a phone and a
desktop, in all three languages:

1. Admin creates a group, a room and the bell schedule, builds a week of timetable for two groups,
   gets a conflict warning when placing a teacher twice, publishes.
2. Teacher logs in, sees the week, opens today's class, marks attendance, opens the lesson in
   presentation mode.
3. Student logs in, sees today's classes, opens the lesson: full theory, examples, code tabs,
   summary, self-check. Runs a code example.
4. Student opens the practice task, writes a solution in Python, runs it with custom input, runs
   the examples, submits, gets a verdict; repeats in Java and in C++.
5. Student takes the quiz and sees the result; the grade appears in Grades.
6. Teacher reviews the submission, leaves feedback, records a grade; the student sees both.
