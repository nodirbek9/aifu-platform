# frontend/AGENTS.md — working notes

The rules are in `../AGENTS.md`. This file is what the next session needs to know about how
this codebase is put together. Keep it current at the end of every phase.

## Commands

```
npm run dev          # http://localhost:5173, /api proxied to http://localhost:8080
npm run lint         # eslint, zero warnings
npm run typecheck    # tsc --noEmit
npm test             # vitest (src/**/*.test.ts[x])
npm run i18n:check   # same keys in uz/ru/en, no unused key, a text for every error code
npm run build
npm run format       # prettier (+ Tailwind class order)
npm run api:types    # regenerate src/shared/api/schema.d.ts from ../docs/api/openapi.json

# against the running backend (docker compose up -d at the repository root):
E2E_DEMO_PASSWORD='<demo password, docs/03-API-GUIDE.md §9>' npm run e2e     # flows: desktop + phone
E2E_DEMO_PASSWORD='…' npm run shots                                           # review screenshots
```

Screenshots land in `e2e/__screenshots__/<phase>/<screen>.<theme>.<locale>.<project>.png`
(git-ignored). Open them and look: that is the review.

## Map

```
src/main.tsx                 entry: providers + router
src/app/providers.tsx        query client, theme, auth, tooltips, toasts
src/app/router.tsx           every route; role sections are children of /student, /teacher, /admin
src/app/guards.tsx           RequireAuth, PublicOnly, ShellRoute, RoleHomeRedirect, RequireRole
src/app/shell/               app-shell, rail (≥768), top-bar, tab-bar (phone), profile-menu, nav.ts
src/app/system-pages.tsx     splash, 404, no access, route error, offline banner
src/app/later-phase-page.tsx placeholder of a section that a later phase builds
src/app/dev-ui-page.tsx      /dev/ui — the design-system catalogue (development builds only)
src/features/<area>/         pages, components, queries.ts (hooks), model.ts (narrowed types):
                             auth, dashboard, courses, lesson, coding, quiz, timetable, grades,
                             attendance, notifications, announcements, search, profile
src/shared/api/              schema.d.ts (generated), client.ts (api, unwrap), session.ts, errors.ts
src/shared/ui/               design-system components; tokens.css is the only place with hex values
src/shared/i18n/             uz.json, ru.json, en.json, index.ts, use-locale.ts
src/shared/lib/              cn, tv, dates, small hooks
e2e/                         support.ts (signIn, texts), *.spec.ts; shots.spec.ts is tagged @shots
```

## Conventions that are easy to miss

- **Calling the API.** `await unwrap(api.GET('/api/v1/…', { params }))` returns the data or
  throws `ApiFailure`; `toApiProblem(error)` turns anything thrown into `{status, code, message,
  details, requestId}`. Hooks live in `features/<area>/queries.ts`; components never call `api`.
- **Narrow once.** Every generated property is optional. A feature's `model.ts` (or `queries.ts`)
  maps the response to a strict type and drops rows that miss what the screen needs.
- **`cn()` and `tv()` come from `@/shared/lib`**, never straight from `tailwind-merge` or
  `tailwind-variants`: only ours know that `text-body` is a size and not a colour. Importing the
  library versions silently drops text colours.
- **Types, not interfaces.** No `any`, no non-null `!`. `exactOptionalPropertyTypes` is off on
  purpose (Radix and React Hook Form prop types are not written for it).
- **i18n.** `t('a.b')` literals or `` t(`prefix.${value}`) `` with a typed value; the checker
  understands both. A key nobody uses fails the check — add keys together with their use.
  Uzbek uses `ʻ` (U+02BB) and `ʼ` (U+02BC). Tests read texts from `e2e/support.ts` → `texts`.
- **Routes.** A role's pages live under its prefix; `nav.ts` lists the sections (rail, tab bar,
  command palette, phone title all read it). `RequireRole` shows "no access" for a role the user
  lacks and switches the active role for one they hold.
- **Navigation after login** is decided by `PublicOnly`, not by the login form.
- **What a feature exposes.** Another feature may import a feature's `model.ts`, `queries.ts` and
  a component meant for reuse (`courses/activity-row.tsx`); pages and their parts stay private.
  The dashboard and the lesson page compose `courses` and `timetable` this way.
- **Focus pages** (the lesson, the practice task, the quiz; later presentation mode) are routes outside
  `ShellRoute`, with their own slim header — see `studentFocusRoutes` in `router.tsx`.
- **The code editor** (`shared/editor/code-editor.tsx`) is heavy: import it with `lazy()`. What
  is light — the language list and names — is in `shared/editor/languages.ts`. Its colours are
  the `--syntax-*` tokens.
- **Runs and submissions** are followed with `useRunQuery` / `useSubmissionQuery`
  (`features/coding/queries.ts`): they poll by `pollInterval` and stop when the job is finished.
- **The quiz** (`features/quiz`): the page keeps the answers while an attempt is open and saves
  them through `useAnswerSaver` (one request at a time per question, newest value wins); the
  clock is `remainingSeconds(attempt, now)` on the server's time. A review is "open" when the
  server sent `isCorrect` — never decide it from the policy in the browser.
- **The week grid** is `features/timetable/week-view.tsx` (`WeekView`): the grid from tablet width
  up, a day strip with the day's list on a phone, and the class sheet. It takes `audience`
  (`student` shows the teacher, `teacher` the groups), the addresses of a lesson and a course, and
  optional `actions` for the sheet — the teacher's timetable and the admin's builder reuse it.
  The table itself is `shared/ui/schedule-grid.tsx`. Parity, week numbers, holidays and moved
  classes are the server's: nothing of that is computed here.
- **The teacher's side** is `features/teaching`: `queries.ts` has every classroom call;
  `TeacherCoursePage` takes `extraSections` so later phases add tabs without touching it
  (`teacher-course-route.tsx` is where they are listed). Presentation mode is a focus page.
- **Excel**: `downloadXlsx(name, sheets)` from `shared/lib/xlsx.ts`; the first row is the header.
- **Pages for every role** (`/notifications`, `/announcements`, `/profile`) are `commonRoutes` in
  the router; the shell shows the navigation of the active role around them.
- **Search** is the command palette: `CommandPalette` takes a `search` prop with the server's
  hits; `searchHitPath(hit, role)` and `notificationPath(item, role)` give an address only where
  the role has a page — add the teacher's and the admin's addresses there in F4 / F5.
- **Dates.** Always through `shared/lib/dates.ts`: campus time zone, and Uzbek written by hand
  (browsers print "M10" for October in Uzbek). Never `toLocaleString` in a component.
- **Untyped JSON** (`payload`, `settings` of a lesson block) is read with `readString` and
  narrowed into a discriminated union in the feature's `model.ts` — see `lesson/model.ts`.
- **State.** Server data only in TanStack Query. Browser preferences in `localStorage` under
  `aifu-*` keys (`aifu-theme`, `aifu-locale`, `aifu-active-role`, `aifu-rail-expanded`,
  `aifu-refresh-token`).
- **Tokens only.** No hex, no `[arbitrary]` values and no inline `style` in features. The few
  arbitrary values in `shared/ui` and the public layout are layout constants of the design.
- **Motion.** State changes: CSS transitions with the duration and easing tokens. Sequences: GSAP
  through `useGSAP` with a scope, and nothing under `prefers-reduced-motion`.
- **E2E.** Real backend, no interception. `signIn(page, account)` first resets the account's
  saved language to Uzbek through the API. Tests run serially (one worker): the refresh token is
  single use. A wrong-password test uses an account that does not exist, so nobody gets locked.
  `e2e/db.ts` reaches the development database (through the Compose Postgres container) for
  what the API cannot do: `e2e/global-setup.ts` clears the demo student's lesson progress and
  submissions before a run; the quiz flows clear their attempts and move an attempt's end closer.

## Decisions made while building (beyond DESIGN-DECISIONS.md)

- Role homes are `/student`, `/teacher`, `/admin`; `/` redirects to the role last used.
- The rail is paper, not navy: the emblem's navy half would vanish on a navy rail.
- On a phone the top bar shows the section's name; the language switch is a compact menu there,
  and the theme and the role switch move into the profile menu.
- The notification list shows a text per notification type: the API sends no title or body
  (see BACKEND-REQUESTS.md).
