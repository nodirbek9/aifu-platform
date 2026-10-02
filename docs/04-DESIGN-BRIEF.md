# AIFU Learning Platform — Design brief

For the designer-developer who builds `frontend/`. `01-PRODUCT-SPEC.md` says what each screen
does; this brief says how the product must look, feel and move. Where the brief gives a value,
it is fixed. Where it gives a direction, the craft is yours — and it is expected to be high.

## 1. The bar

The owner asked for one thing above all: **it must look like the platform of a major university**
— the level of MIT OpenCourseWare, Harvard, Stanford Online, ETH Zürich, edX — and not like a
template admin panel. Two earlier attempts were thrown away because they looked generic.

Study those sites before you draw anything, and notice what makes them feel like institutions:

- **Typography carries the page.** Large, confident serif headlines; calm, readable body text;
  few sizes, used consistently. Hierarchy comes from type and space, not from boxes.
- **Restraint.** One or two brand colours, a lot of quiet surface, colour used to mean something.
- **A strict grid and generous space.** Things line up. Nothing is cramped; nothing floats.
- **Density where work happens.** Timetables, gradebooks and journals are compact and exact,
  like a well-set printed table.
- **Details are finished.** Empty states, focus rings, loading, long names, the 390 px layout,
  the dark theme — all designed, none left to defaults.

Do not copy any of them. Design something that could only be AIFU's.

## 2. Concept: a university press, on screen

The interface should feel like a well-made academic publication that happens to be interactive:
a lesson reads like a chapter of a good textbook, a timetable like a printed schedule, a grade
sheet like an official record — and the code editor like a real development tool.

Three ideas hold the design together. Use them; add your own.

1. **The shield.** AIFU's emblem is a shield: square shoulders, a round base, split navy over
   magenta. That silhouette and that two-part split are the brand's own shapes. Use them with
   intent — a mask for the login image, the avatar frame, the shape of an achievement or a
   course emblem, the two-tone divider of a hero. Sparingly: a motif, not a pattern on
   everything.
2. **Slab serif headlines.** The emblem's letters are a slab serif. Headlines in a slab serif
   make every page look like it belongs to the same institution as the logo.
3. **Magenta means "now".** Navy builds the structure. Magenta is the single accent, reserved
   for the one thing that matters at this moment: the primary action, the class happening now,
   today's column, the current step. If magenta is in more than a few places on a screen, it
   has stopped meaning anything.

## 3. Brand assets (real, use these)

| Asset | File | Notes |
|---|---|---|
| Emblem | `academic-materials/branding/AIFU_logo.png` | 1080×1080, transparent. Copy into `frontend/src/assets/brand/`. Redraw it as an SVG for crisp rendering (shield, split, the letters `AI` / `FU`); keep the proportions exactly. Never recolour, stretch or put it on a busy background |
| Campus building | `academic-materials/branding/photo_2026-09-23_16-57-59.jpg` | 1280×615, the only real photograph. Low resolution: use it large only under a navy duotone / gradient treatment (login page), never as a sharp hero |
| Name | Aniq va Ijtimoiy Fanlar Universiteti · short: AIFU | Est. 2022. The Uzbek name is official. For `ru` (Университет точных и социальных наук) and `en` (University of Exact and Social Sciences) use these translations and list them in `DESIGN-DECISIONS.md` for the owner to confirm |

There are no other photographs, mascots or illustrations. Do not use stock photos or
AI-generated people or campuses. Build atmosphere from type, the shield geometry, the grid, fine
lines, subtle texture and motion.

## 4. Colour

Brand values, measured from the emblem — fixed:

| Token | Value | Use |
|---|---|---|
| `brand-navy` | `#002E60` | Structure: headlines on light, primary surfaces of the shell, key lines |
| `brand-magenta` | `#FF0158` | The accent, as fills and large shapes |

Build the rest of the palette around them as semantic tokens (`surface`, `surface-raised`,
`surface-sunken`, `text`, `text-muted`, `border`, `primary`, `primary-foreground`, `accent`,
`accent-foreground`, `success`, `warning`, `danger`, `info`, …) defined once in `@theme` for
light and once for dark. Components use only semantic tokens, so the theme flips in one place.

Rules:

- **Contrast.** Text ≥ 4.5 : 1, large text and UI parts ≥ 3 : 1, in both themes. Pure
  `#FF0158` on white is 3.9 : 1: fine for fills and large type, not for body text or for white
  text on a magenta button. Derive a darker magenta step for those (about `#D8004B`, 5.2 : 1)
  and a lighter one for the dark theme. Check every pair; do not trust the eye.
- **Neutrals are cool and slightly navy-tinted**, not pure grey. Light theme surfaces are warm-
  white paper, not `#FFFFFF` everywhere; the dark theme is deep navy-black built from the brand
  navy, not neutral `#111`.
- **Status colours only mean status** (success, warning, danger, info). Never decoration.
- **Session types have their own six colours**, used identically in the timetable, the course
  plan, badges and legends: lecture, seminar, practical, laboratory, independent study, course
  work. Choose six hues that stay distinguishable for colour-blind users, each with a tint
  (cell background), a strong step (left bar, text) and dark-theme values. None of them is the
  accent magenta. Colour never works alone: every badge also has a label or an icon.
- **Verdicts**: accepted = success; partial = warning; wrong answer, runtime error, time limit,
  memory limit, compilation error = danger family, told apart by icon and label.
- Charts use the same tokens: one navy series by default, the accent for the highlighted one.

## 5. Typography

Four roles. Fonts are self-hosted with `@fontsource` (variable versions); no CDN.

| Role | Requirement | Recommended |
|---|---|---|
| Display — headlines, numbers that matter | Slab serif echoing the emblem | Bitter |
| Reading — lesson prose | A text serif made for long reading | Literata or Source Serif 4 |
| Interface — navigation, forms, tables | A clear sans with first-class Cyrillic | Golos Text or Onest |
| Code | Monospace, ligatures off | JetBrains Mono |

You may propose a different pairing if it is better; record the reason in
`frontend/DESIGN-DECISIONS.md`. Whatever you choose **must** pass this test in every weight you
use, with no fallback glyph: `Oʻzbekiston · gʻoya · maʼlumot · Ўзбекистон · Щёлочь · Qq Gg 0O 1lI`.
(Uzbek Latin uses U+02BB `ʻ` and U+02BC `ʼ`; Russian needs full Cyrillic.)

Rules:

- A modular scale with few steps (about eight), defined as tokens with their line height and
  tracking. Headlines tight (−1 to −2 %), body relaxed (1.6–1.75 in lessons).
- Lesson text column 65–72 characters wide; 18–19 px on desktop, 17 px on a phone.
- `text-wrap: balance` on headlines, `pretty` on paragraphs; tabular figures in every table,
  timetable, timer and score; real quotation marks and dashes per language (« » in Russian and
  Uzbek, “ ” in English).
- Russian and Uzbek strings run 20–35 % longer than English. Design with the longest string:
  nothing truncates in navigation or buttons; long subject names wrap to two lines by design.

## 6. Space, shape, depth

- 4 px base unit; a spacing scale as tokens. Page gutters 16 px (phone), 24 px (tablet), 32–48 px
  (desktop). Content width capped (about 1200–1320 px); the lesson column narrower.
- Radii are small and consistent (a document, not a toy): about 6 px controls, 10–12 px cards,
  and concentric where nested (inner radius = outer − padding).
- Depth mostly from borders and surface steps. Shadows are rare, soft, navy-tinted, and reserved
  for things that really float: menus, dialogs, the dragged timetable card.
- Lines are 1 px and quiet. Avoid cards inside cards; group with space and headings first.
- Hit areas ≥ 44 px on touch. Focus ring visible, 2 px, offset, in every theme.

## 7. Layout

**App shell.** Desktop ≥ 1024 px: a left rail (collapsed 72 px / expanded about 264 px, remembered)
with the emblem, the role's sections and the role switch for accounts with several roles; a slim
top bar with the page title or breadcrumb, search (Ctrl+K command palette), language switch
(UZ / RU / EN, always visible — spec G-01), theme switch, notifications and the profile menu.
Phone: a top bar and a bottom tab bar with the role's four or five main sections; everything else
under "More". The lesson page, the task page and presentation mode hide the shell chrome to give
the work the whole screen.

**Page anatomy.** A page header (title in the display face, one line of context, the primary
action on the right), then content on a 12-column grid. One primary action per screen.

**Tables.** Sticky header and first column, zebra off, row hover, numbers right-aligned in
tabular figures, compact / comfortable density switch on the big grids. On a phone a table
becomes stacked rows or scrolls inside its own container with a visible edge fade — the page
itself never scrolls sideways (spec G-02).

## 8. Motion

Motion explains what happened; it never decorates. Tokens for duration (about 120 / 200 / 320 /
600 ms) and easing (one standard ease-out, one emphasised, one spring-like for drag). Animate only
`transform` and `opacity`. Everything is off or reduced to a fade under
`prefers-reduced-motion`. CSS transitions for state changes; GSAP for sequences.

The moments that deserve choreography — make these excellent and leave the rest quiet:

1. **Login**: the shield assembles (two halves meet), the name sets in, the form follows.
2. **First load of a dashboard**: blocks rise in a short stagger, once — not on every visit to
   the tab, never on data refetch.
3. **Route changes**: a 200 ms cross-fade with a few pixels of travel (View Transitions API);
   lesson → next lesson slides in reading direction.
4. **Progress**: rings and bars animate to their value; a completed lesson draws its check.
5. **The verdict** (the product's best moment): after Submit, the status moves queued → running
   with a live indicator; then tests tick in one by one, pass or fail; then the verdict lands —
   Accepted with a restrained celebration in brand colours, anything else calm and helpful.
6. **Timetable builder**: the dragged class lifts with a shadow; free slots invite; a conflict
   shakes the card once and names the clash; a successful drop settles.
7. **Reading**: the progress bar at the top of a lesson; the table of contents follows the
   section in view.
8. **Micro-interactions**: press feedback (scale 0.97), hover on rows and cards, icon
   cross-fades (copy → copied), number count-up for scores. Fast, interruptible.

## 9. Screens — direction, not wireframes

**Login (the public face).** The one page everyone sees, so it carries the brand. Desktop: two
panes — the brand pane (campus photo under a navy duotone, masked or framed by the shield
geometry, the emblem, the university name in the display face, the split navy / magenta) and the
form pane (username, password with show/hide, language switch, clear errors by error code).
Phone: the brand collapses to a compact header above the form. No invented slogans or numbers.
The forced password change and "no access" pages share this language.

**Student dashboard (S-01).** A greeting line with the name and group, set like a headline —
then "today" as the hero: the classes of the day as a timeline with the current or next one
marked in magenta, the room and the teacher. Beside or below: continue learning (one strong card
with progress), deadlines, recent grades. Four blocks that load independently, each with its own
skeleton and empty state.

**Timetable (S-02, T-02).** The piece people will open every day; make it beautiful and exact.
Days Mon–Sat × pairs with the bell times in the left column; class cards coloured by session
type with subject, room, teacher or group; today's column tinted, the current pair marked, the
"now" line across the grid. The header shows the week dates and **toq / juft** (odd / even) as a
clear chip. Cancelled and moved classes are visible, struck through with the reason; a moved
class shows where it went. Phone: a day strip to swipe between days and a list of the day's
classes. A print stylesheet makes a clean A4 landscape page (T-02.3).

**Course page (S-03).** A header with the subject name in the display face, code, teachers,
term, a progress ring and "Continue". Tabs: Plan (modules as sections; lessons as rows with
type colour, hours, status, lock), Syllabus (outcomes, hour budget, the JN / ON / YaN scheme as
a clear weighted diagram), Work, Grades.

**Lesson page (S-04) — the core of the product.** A chapter of a fine textbook. Chrome
recedes: a reading column in the reading serif, generous leading, a sticky table of contents on
the right (a drawer on the phone), a reading progress line at the top. Objectives in a set-off
block at the start. Headings in the display face with anchor links. Code examples as designed
objects: language tabs (Python / Java / C++) that remember the choice across the whole app, line
numbers, a copy button, "Try it" opening a runner in place. Callouts (note, tip, warning,
danger) distinct by icon and colour bar, not by loud backgrounds. Tables set like print. Images
and diagrams with captions, click to zoom. Self-check questions reveal their answer. The lesson
ends with practice (the task and the quiz with their status), "Mark as done", and previous /
next. A "shown in Uzbek" mark appears when the content fell back to another language.

**Practice task (S-05).** An IDE, not a form. Two resizable panes: statement (description,
input and output format, constraints, examples with copy, hints revealed one at a time) and the
editor with a toolbar (language, font size, reset, full screen), then a result panel with tabs
(Output, Examples, Submission, History). Dark editor themes in both app themes are acceptable
if they are designed to match. Keyboard shortcuts are shown on the buttons. Phone: three tabs —
Task, Code, Result — with Run and Submit pinned at the bottom.

**Quiz (S-06).** Calm and focused: one question per screen, a question navigator showing
answered / unanswered, a timer that turns to the warning colour in the last minutes, saving
state visible ("saved"), a deliberate submit confirmation, and a result page that respects the
teacher's review policy.

**Grades (S-08).** An official record: components JN / ON / YaN with weights, earned / maximum,
pass gates and admission shown as a clear statement, final score and grade in the display face.
History of changes is available without clutter.

**Teacher — class session (T-03).** Used live, standing at a desk: big clear header (subject,
groups, room, time), the topic of the class with its lesson, attendance as a fast list with
four states and "all present", notes that save as you type. **Presentation mode** is a separate
full-screen design for a projector: very large type, high contrast, no chrome, arrow keys and
on-screen arrows between sections, code readable from the back of the room.

**Teacher — editors (T-04, T-05, T-06).** Editing tools: a tree or list on the left, the
editor in the middle, a live preview as the student sees it. Language tabs UZ / RU / EN on each
translatable field with a marker where a translation is missing. Autosave state always visible.

**Gradebook and journal (T-08, T-10).** Dense grids with sticky headers, keyboard navigation
between cells, totals and percentages, export to Excel.

**Admin — timetable builder (A-02).** The most complex screen. Three views (by group, teacher,
room) of one grid; a side panel for adding and editing a class and for the load (planned versus
scheduled hours, with what is still unscheduled); drag and drop with a live conflict check;
conflicts name the clashing class; warnings can be overridden with a reason; draft and published
states unmistakable. Design it for a 1440 px desktop first; on a phone it is a read-mostly day
list with an edit sheet.

**Admin — reference data, users, academic structure (A-03…A-07).** Consistent list → detail →
form patterns: searchable, filterable tables; forms in side sheets or dialogs with field-level
errors; import with a dry-run preview row by row before anything is saved.

**System pages.** 404, no access, session expired, offline, maintenance — designed, in the
brand language, each with a way forward.

## 10. Components to build once (in `shared/ui`)

Button · icon button · link · input · textarea · select · combobox · checkbox · radio · switch ·
date picker · segmented control · tabs · badge (status, session type, verdict) · avatar (shield
frame) · card · stat · progress bar and ring · table and data grid · pagination · skeleton ·
empty state · error state · toast · dialog · sheet / drawer · dropdown menu · tooltip · popover ·
command palette · breadcrumb · page header · language switch · theme switch · code block ·
code editor · callout · diff view · timeline · calendar grid · file upload · rich text (HTML)
renderer.

Each has every state (default, hover, focus, active, disabled, loading, error) in both themes.
Build a `/dev/ui` route (development builds only) that shows all of them: it is how the design
system is reviewed.

Icons: one set, one stroke weight (Lucide or Phosphor). No emoji as icons.

## 11. Do not

- Generic "AI" looks: purple or blue-violet gradients, glass cards on blurred blobs, neon glows.
- The default shadcn / Bootstrap / Material appearance left as it comes.
- Cards inside cards inside cards; a shadow on everything; a border and a shadow together.
- Emoji as icons or decoration; waving-hand greetings; exclamation marks in interface text.
- Stock or generated photos and illustrations of people; invented statistics, slogans, awards.
- Rainbow charts; colour as the only carrier of meaning.
- Spinners where a skeleton belongs; blank screens; layout that jumps when data arrives.
- Centre-aligned everything; text over busy images without a treatment; truncated navigation.
- Animation on every element, on every render, or that delays the user.
- Lorem ipsum or placeholder data anywhere, including screenshots and tests.

## 12. How the design is accepted

For each screen, screenshots at 390, 768 and 1440 px, in light and dark, in `uz`, `ru` and `en`,
with real data from the demo database, are compared against this brief. The owner's question is
simple: "does this look like a serious university's platform, and does everything work?"
Record the choices you made (fonts, scale, palette steps, layout decisions, motion) in
`frontend/DESIGN-DECISIONS.md` so the next phase stays consistent with the first.
