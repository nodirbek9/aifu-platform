# Frontend build — progress

The single record of progress (AGENTS.md §8). A new session resumes from this file alone.

| Phase | What | Status |
|---|---|---|
| F0 | Foundation: design system, shell, auth | **reported** 2026-10-02 (the owner asked to go on to F1 without waiting) |
| F1 | Student: dashboard, courses, lesson | **in progress** |
| F2 | Student: practice task, quiz, work list | not started |
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

## F1 — Student: dashboard, courses, lesson (in progress)

Spec: S-01, S-03, S-04 (S-04.4 "Try it" belongs to F2). Demo account: student3.
Plan and state of the work are kept below as the phase goes on.
