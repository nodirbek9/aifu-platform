# AIFU Learning Platform — frontend

The web app of the platform: students, teachers and the study office in one React app, in
Uzbek, Russian and English, light and dark, from a 390 px phone to a 1440 px desktop.

## Run it

Needs Node 24 and the backend (`docker compose up -d` at the repository root: Postgres, the API
on `http://localhost:8080`, the code execution worker).

```bash
npm ci
npm run dev            # http://localhost:5173 — /api is proxied to :8080
```

Demo accounts (development data only): `student3`, `n.ayitbayev` (teacher and administrator),
`admin`; the password is in `docs/03-API-GUIDE.md` §9.

## Check it

| Command | What it proves |
|---|---|
| `npm run lint` | ESLint (type-aware, React Compiler rules), zero warnings |
| `npm run typecheck` | TypeScript, strict |
| `npm run test` | Vitest: models, the API client, the .xlsx writer and reader, i18n parity |
| `npm run i18n:check` | the three catalogues have the same keys, none unused, every API error code |
| `npm run build` | the production bundle in `dist/` |
| `E2E_DEMO_PASSWORD=… npm run e2e` | Playwright flows against the real backend, desktop and phone, on the production build (`vite preview` on :4173, started for you) |
| `E2E_DEMO_PASSWORD=… npm run shots` | review screenshots per phase into `e2e/__screenshots__/` |

`E2E_BASE_URL=http://localhost:3000 npm run e2e` runs the same flows against another build, such
as the nginx image below. The flows reset the demo data they change through the Postgres
container (`e2e/db.ts`); set `E2E_DB_CONTAINER=none` to skip that.

Among the flows: the six steps of the acceptance demo (spec §9), axe (WCAG 2.1 AA) on every
route of every role in both themes (`e2e/a11y.spec.ts`), and every screen at 390 px with nothing
wider than the screen (`e2e/layout.spec.ts`).

## Ship it

```bash
docker build -t aifu-frontend .
```

A multi-stage build: Node builds, `nginx-unprivileged` serves (`nginx.conf`): the SPA fallback,
`/api` proxied to the `api` service (same origin — no CORS in production), hashed assets cached
for a year and `index.html` never, security headers with a strict Content-Security-Policy (no
inline scripts; the pre-paint theme script is `public/theme-init.js`), uploads up to 55 MB, and
`/healthz`. The compose service block is proposed in `BACKEND-REQUESTS.md`.

## How it is built

```
src/
  app/          router (every page a lazy chunk), shell (rail, top bar, bottom bar), guards
  features/     one folder per area: a page, its components, model.ts (narrowing), queries.ts
    auth  dashboard  courses  lesson  coding  quiz  timetable  grades  attendance
    notifications  announcements  search  profile                              the student
    teaching  authoring  review  gradebook  analytics                            the teacher
    admin  builder                                                               the study office
  shared/
    api/        openapi-fetch client generated from docs/api/openapi.json, session, errors
    ui/         the design system (Radix + tailwind-variants), tokens.css
    editor/     CodeMirror 6 (Python, Java, C++), loaded only where code is shown
    i18n/       uz / ru / en catalogues
    lib/        dates on the campus clock, cn/tv, .xlsx writer and .xlsx/.csv reader, hooks
```

Decisions worth knowing (the details are in `AGENTS.md` and `DESIGN-DECISIONS.md`):

- **The contract is the source of truth.** Types come from the backend's OpenAPI document
  (`npm run api:types`); API data is narrowed once in a feature's `model.ts`, so components
  never meet an optional field they did not expect. What the API lacks is asked for in
  `BACKEND-REQUESTS.md`, never faked.
- **Server state lives in TanStack Query**; URLs hold what should survive a reload or a shared
  link (tabs, filters, pages, the builder's view); nothing important lives in local storage.
- **Errors are codes.** Every failure is an `ApiProblem` with the server's error code; the UI
  translates the code, never the message.
- **The session**: a 15-minute access token in memory, the refresh token in local storage,
  refreshed once per expiry and serialised across tabs with the Web Locks API (the refresh
  token is single-use; two tabs never spend it twice).
- **Resilience**: every screen has its skeleton, empty and error state (with retry and the
  request id); a request that meets an expired token is refreshed and replayed; submissions
  carry an idempotency key, so a double click or a reload never submits twice, and the history
  shows what was sent; editors save after a pause, in order, and say so; a change made
  elsewhere (409) offers a reload instead of overwriting it.
- **Heavy things load late**: the code editor, GSAP, the form library and the sign-in page are
  separate chunks; a signed-in visit starts with React, the router, the query client and the
  shell (about 135 kB gzipped).
- **Accessibility** is tested, not assumed: axe on every route, keyboard paths in the flows
  (attendance, presentation mode, the gradebook grid, the timetable builder's drag by keyboard),
  `prefers-reduced-motion` honoured everywhere.
