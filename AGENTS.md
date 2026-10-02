# AGENTS.md — AIFU Learning Platform

Rules for coding agents in this repository. Two kinds of work happen here, each with its own
rule file:

| You are working on | Your rules |
|---|---|
| `frontend/` — the web app | **This file** |
| `backend/`, `docker/`, `tools/`, database, API | `CLAUDE.md` |

The rest of this file is for the **frontend agent**.

## 1. Your job

Build `frontend/`: the web application of the learning platform of AIFU (Aniq va Ijtimoiy Fanlar
Universiteti, Uzbekistan) for three roles: student, teacher, administrator. The backend is
finished, tested and running; you build the interface on top of its real API.

The owner's bar, in his words: it must look **as good as the platforms of the big universities**
— beautiful, modern, polished — and everything on screen must really work. Two earlier frontends
were rejected for looking generic and for showing fake data. There is no earlier frontend code in
this repository on purpose: design it yourself, from the brief.

## 2. Read before you write (in this order)

1. `docs/01-PRODUCT-SPEC.md` — what every screen must do. Each feature has an ID (`S-04.2`,
   `T-03.1`, `A-02.4`) with acceptance criteria. This is the source of truth for behaviour.
2. `docs/04-DESIGN-BRIEF.md` — how it must look, feel and move. The source of truth for design.
3. `docs/03-API-GUIDE.md` — how to call the backend, and which endpoints each screen uses.
4. `docs/api/ENDPOINTS.md` — every endpoint in one line; `docs/api/openapi.json` — the contract.
5. `docs/05-FRONTEND-PROMPTS.md` — the build phases F0–F6. You are given one phase at a time.

Project skills in `.qoder/skills/` carry the detailed rules (`aifu-design-system`, `aifu-api`,
`aifu-screen-done`, plus Tailwind, GSAP, typography, motion and type-safety skills). Load the
ones that match the task before you start it.

## 3. Hard rules

1. **Write only inside `frontend/`.** Never edit `backend/`, `docker/`, `docker-compose.yml`,
   `docs/`, `tools/`, `academic-materials/`, `.env`. You may read all of them except `.env`.
2. **No mock data, ever.** No hard-coded lists, no fake numbers, no placeholder names, no
   `setTimeout` pretending to be a server, no "demo mode". Every value on screen comes from the
   API. If the API cannot give a screen what it needs, add an entry to
   `frontend/BACKEND-REQUESTS.md` (screen, missing data, proposed endpoint), build the rest of the
   screen, and show an honest empty state for the missing part.
3. **Types come from the contract.** Generate them from `docs/api/openapi.json` with
   `openapi-typescript`; call the API only through the typed `openapi-fetch` client in
   `src/shared/api`. Never hand-write a response type and never use `any` or `as` to silence a
   mismatch — a mismatch means you misread the contract.
4. **Never invent facts about the university**: no made-up statistics, rankings, slogans,
   addresses, staff names, photographs of "campus". Use the logo, the name and what the API returns.
5. **Three languages.** No user-visible string in a component. Everything goes through `t()`;
   `uz`, `ru` and `en` have identical keys. Uzbek is Latin script; write `oʻ` and `gʻ` with
   U+02BB (`ʻ`), not an apostrophe.
6. **Every screen has four states**: loading skeleton, empty state with a next action, error
   state with retry and the `requestId`, and content.
7. **Every screen works at 390, 768 and 1440 px**, in light and dark theme, by keyboard, and with
   `prefers-reduced-motion`.
8. **Do not report work as done unless you ran it.** "Done" means: the quality gates in §6 pass,
   and you opened the screen in a browser against the running backend and looked at it (see the
   `aifu-screen-done` skill). Say plainly what you did not verify.
9. **Secrets.** Never read or print `.env`. No tokens, passwords or keys in code or logs. The demo
   password appears only in `docs/03-API-GUIDE.md` and in e2e test config read from env.
10. **Do not push, deploy or publish anything.** Commit locally only (see §7).

## 4. Stack (fixed — do not swap parts without asking the owner)

React 19 · TypeScript (strict, `noUncheckedIndexedAccess`) · Vite · Tailwind CSS 4 (tokens in
`@theme`) · shadcn/ui on Radix primitives, restyled to the design system · `tailwind-variants`
for our own component variants · React Router 7 (SPA, lazy routes, view transitions) · TanStack
Query 5 · React Hook Form + Zod · i18next + react-i18next · CodeMirror 6 (python, java, cpp —
also used read-only for code in lessons) · dnd-kit (timetable builder) · Recharts (analytics) ·
GSAP + `@gsap/react` for sequenced motion, CSS transitions for state changes (no other animation
library) · `openapi-typescript` + `openapi-fetch` · DOMPurify · `read-excel-file` +
`write-excel-file` (lazy-loaded) for Excel; printing through a print stylesheet · fonts
self-hosted through `@fontsource` (no CDN) · Vitest + Testing Library · Playwright.

Package manager: npm. Node 22 or newer. Use the current stable version of each package; pin
exact versions in `package.json`.

## 5. Structure

```
frontend/
├── AGENTS.md                frontend-local notes you keep up to date (commands, decisions)
├── BACKEND-REQUESTS.md      what the API lacks (rule 2)
├── DESIGN-DECISIONS.md      your design choices and why (fonts, scale, layout, motion)
├── e2e/                     Playwright flows of the acceptance demo
└── src/
    ├── app/                 router, role guards, providers, app shell
    ├── features/<area>/     one folder per spec area: pages, components, hooks, schemas
    └── shared/
        ├── api/             generated types, client, auth + refresh, error mapping
        ├── ui/              design-system components (the only place raw styles live)
        ├── editor/          CodeMirror wrapper
        ├── i18n/            uz.json, ru.json, en.json
        └── lib/             dates (Asia/Tashkent), formatters, storage
```

- A feature owns its pages, components, `useXxxQuery` / `useXxxMutation` hooks and Zod schemas.
  Features never import each other's internals; shared code moves to `shared/`.
- Components never call `fetch`. Server state lives in TanStack Query only; query keys include
  the locale for translated content. No copy of server data in `useState` or a global store.
- Pages are lazy routes. The code editor, charts, Excel and GSAP load only where they are used.
- Colours, spacing, radii, shadows, fonts and durations come from tokens. No hex value, no
  arbitrary `[…]` Tailwind value and no inline style outside `shared/ui` and the token file.

## 6. Quality gates (all must pass before you say a phase is done)

```
cd frontend
npm run lint          # eslint, zero warnings
npm run typecheck     # tsc --noEmit, strict
npm test              # vitest
npm run i18n:check    # uz / ru / en have the same keys, no unused, no missing
npm run build         # production build
npm run e2e           # Playwright, desktop and phone projects, against the running backend
```

The backend runs with `docker compose up -d` at the repository root (the owner starts it):
API at `http://localhost:8080`, health at `/actuator/health`. Vite proxies `/api` to it. Demo
accounts are in `docs/03-API-GUIDE.md` §9.

## 7. Working agreement

- One phase at a time, in the order of `docs/05-FRONTEND-PROMPTS.md`. Inside a phase: plan →
  build → verify in the browser → fix → report.
- Before building a screen, write down for yourself: the spec IDs it covers, the endpoints it
  uses (from the guide), its states, and its layout at 390 / 768 / 1440.
- Commit locally after each finished screen: `feat(frontend): <what> (<spec ids>)`. Never commit
  `node_modules`, build output, `.env` or screenshots outside `frontend/e2e/__screenshots__`.
- End each phase with a short report: spec IDs done, what was verified and how, what is not done,
  entries added to `BACKEND-REQUESTS.md`, and design decisions added to `DESIGN-DECISIONS.md`.
- If the spec, the brief and the API disagree, stop and ask; do not pick silently.
