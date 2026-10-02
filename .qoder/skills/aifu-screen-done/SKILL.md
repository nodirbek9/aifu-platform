---
name: aifu-screen-done
description: 'Definition of done for a screen of the AIFU frontend and how to verify it for real — quality gates, browser check against the running backend with the demo accounts, screenshots at three widths in both themes and three languages, accessibility, and the phase report. Use before saying any screen, feature or phase is finished. Triggers on: done, finished, complete, verify, check, test, review, screenshot, report, acceptance, e2e, Playwright.'
---

# Is this screen done?

A screen is done when every line below is true and you have seen it yourself. If you could not
check something, the report says so in plain words. Never write "works" for something you did
not run.

## 1. It does what the spec says

- Every acceptance line of its spec IDs in `docs/01-PRODUCT-SPEC.md` is implemented. List the
  IDs and tick each one.
- Every value on screen comes from the API. Search your own diff for hard-coded names, numbers,
  dates, arrays of objects and `setTimeout`: there must be none outside tests.

## 2. Gates pass

```
npm run lint && npm run typecheck && npm test && npm run i18n:check && npm run build
```

Zero warnings. No `any`, no `as` casts to silence types, no `// @ts-ignore`, no
`eslint-disable` without a written reason.

## 3. You looked at it

The backend must be up (`curl http://localhost:8080/actuator/health` → `UP`; if it is not,
stop and tell the owner — do not fake it). Demo accounts are in `docs/03-API-GUIDE.md` §9.

Open the screen with the right demo account and take screenshots with Playwright:

| Width | Themes | Languages |
|---|---|---|
| 390 × 844 | light, dark | uz, ru, en |
| 768 × 1024 | light | uz |
| 1440 × 900 | light, dark | uz, ru |

Save them under `frontend/e2e/__screenshots__/<screen>/`. **Open the image files and look at
them.** Check each against `docs/04-DESIGN-BRIEF.md` and the self-review questions of the
`aifu-design-system` skill. Fix what you see; shoot again.

Look specifically for: horizontal page scroll · clipped or overlapping text (Russian is the
longest) · a missing translation showing a key like `course.plan.title` · unstyled or default-
looking controls · inconsistent spacing · low contrast in the dark theme · layout jumping when
data arrives · an empty area where an empty state should be.

## 4. The four states exist — and you saw each

- **Loading**: a skeleton in the shape of the content (throttle the network to see it).
- **Empty**: use a demo account that has no such data; there is a message and a next action.
- **Error**: stop the API container or block the request; there is a message, a retry button and
  the request id — and retry works when the API is back.
- **Content**: with real seeded data, including long names.

## 5. It can be used

- Keyboard only: every control reachable in a sensible order, focus always visible, dialogs trap
  and return focus, Escape closes.
- Labels on every input; icons that are buttons have an accessible name; images have `alt`.
- `prefers-reduced-motion`: no movement beyond fades.
- No console errors or warnings while using the screen. No failed requests in the network log
  except the ones you provoked.
- Reload on the screen keeps the user there, signed in. A deep link opened signed out returns to
  it after login. The browser back button behaves.

## 6. It is covered

- Unit tests for the logic that can be wrong (mappers, formatters, state machines such as the
  submission status, the i18n parity check).
- A Playwright flow for every step of the acceptance demo (`01-PRODUCT-SPEC.md` §9) the screen
  belongs to, on the desktop and the phone project, against the real backend — no request
  interception that fakes responses.

## 7. The report

End the work with:

```
## Phase <id> report
Spec IDs done: …
Verified: <what you ran and saw — gates, accounts used, screenshots taken>
Not verified / not done: … (and why)
Backend requests added: … (or none)
Design decisions recorded: … (or none)
Known issues: …
```

Honest and short. A gap that is reported is a task; a gap that is hidden is a defect.
