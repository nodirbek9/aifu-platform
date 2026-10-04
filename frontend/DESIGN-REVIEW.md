# Design review (F6)

How it was done: screenshots of every phase's screens at 1440 and 390 in both themes
(`npm run shots`, `e2e/__screenshots__/`), looked at one by one; and three automated sweeps that
turn a reviewer's eye into checks that run on every change: axe on every route of every role in
both themes (`e2e/a11y.spec.ts`), every screen at 390 px with nothing wider than the screen
(`e2e/layout.spec.ts`, `admin-records`, `admin-builder`), and every screen in Russian and English
on desktop and phone with no untranslated key (`e2e/locales.spec.ts`).

Ranked by how much a user would feel it. **Fixed** means fixed and re-checked by the sweep or a
new screenshot; **open** means still there.

## 1. Pages wider than a phone — fixed

The worst defect found: on a 390 px phone several pages scrolled sideways — the teacher
dashboard (a long name, a column of cards), the course tabs, the curriculum tree, the quiz and
lesson editors, the users list (two header buttons), a course's practice tab in Russian ("Новая
задача" next to the title) and the student's course page in Russian and English (six tabs). The
old checks could not see it: they compared the page with `innerWidth`, which grows with the
content on an emulated phone.
Fix: grids get a shrinkable column (`grid-cols-1`), `Tabs` never widen their parent and their list
scrolls, `PageHeader` and `CardHeader` actions wrap under the title, long names break
(`wrap-anywhere`). The sweeps measure against the device width.

## 2. Contrast — fixed

axe found: select placeholders at 70 % of the muted colour; code strings, numbers and comments on
the highlighted line of the code examples; past classes on the dashboard, cancelled classes in
the week and the thematic plan dimmed with opacity until their text failed 4.5:1.
Fix: placeholders in the full muted colour; the syntax colours darkened (light theme) and
lightened (dark) to pass on the active line; dimmed rows now use the sunken surface, the line
through and the status badge instead of opacity.

## 3. Things a screen reader could not name — fixed

Progress bars without a name (grades, the syllabus hour plan, presentation mode); the code
viewer's scroller could not take the keyboard focus; read-only timetable cards were announced
"disabled" by the drag library.

## 4. Russian runs long — fixed where it broke the layout

The locale sweep found no clipped text besides §1. Long Russian labels wrap inside buttons and
tabs; the course tabs scroll.

## 5. Motion — done

The eight moments of the brief: the sign-in shield (F0), the dashboards rising in turn on the
first visit of a session (new, `rise-in` with the emphasised easing, never on a refetch), route
changes with View Transitions, progress animating to its value, the verdict sequence (F2), the
timetable builder (the class lifts, cells turn green / amber / red as the check answers, a
refused drop shakes once), the reading progress line, press feedback. All of it off under
`prefers-reduced-motion` (durations and delays to zero).

## 6. Open

- The rich-text fields of the editors are HTML with a toolbar and a live preview, not a WYSIWYG
  editor: fine for the platform's teachers today, the first thing to improve for others.
- The gradebook's sticky first column has a slightly different surface from its row in dark
  theme.
- The load panel of the builder shows over-scheduled lines (36 of 30 h) without a warning.
- On a slow mobile network the first paint waits for the app's JavaScript (Lighthouse mobile
  performance about 70 on the sign-in page, 97 on desktop): a server-rendered sign-in page would
  be the next step.
