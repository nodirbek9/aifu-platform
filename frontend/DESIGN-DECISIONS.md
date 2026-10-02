# AIFU Learning Platform — Design decisions

Phase F0, step 1. This file records the look of the product: concept, fonts, tokens, shell,
motif and the login page. It is the contract every later phase builds against. Status:
**approved by the owner on 2026-10-02**; §10 lists what changed while it was built.

---

## 1. Concept — a university press, on screen (five sentences)

1. AIFU's platform reads as a university press that happens to be interactive: pages are set
   like academic print — a quiet paper surface, one ink, one accent — with the exactness of a
   well-composed timetable or grade record.
2. The shield's own geometry (square shoulders, round base, the navy-over-magenta split)
   supplies the only ornament: a mask for the login image, the avatar frame, one two-tone
   divider — a motif, never a pattern.
3. Bitter slab-serif headlines tie every page to the emblem; Literata carries long lessons;
   Onest keeps the interface crisp in all three scripts; Source Code Pro keeps code honest.
4. Navy builds the structure; magenta appears only where "now" is — one primary action, the
   current class, today's column — so its rarity is the meaning.
5. Depth comes from surface steps and hairlines rather than shadows, and motion is reserved
   for the moments that explain something (the verdict reveal, the timetable) — everything
   else is still.

---

## 2. Typography

### 2.1 The pairing

| Role | Font | Why |
|---|---|---|
| Display — headlines, page titles, numbers that matter | **Bitter** (variable) | The slab serif of the emblem's AI/FU letters; every headline looks like it belongs to the same institution as the logo |
| Reading — lesson prose, long content | **Literata** (variable, wght) | A text serif designed for long reading on screens; calm at 18 px |
| Interface — navigation, forms, tables, buttons | **Onest** (variable) | Cyrillic-first sans, designed for exactly our language pair; clean at small sizes |
| Code | **Source Code Pro** (variable), ligatures off | A sober, highly legible text monospace |

All self-hosted through `@fontsource-variable/*` (no CDN). Weights used: Bitter 500/600/700,
Literata 400/500/600 (+ italic 400), Onest 400/500/600/700, Source Code Pro 400/500/600.

### 2.2 Glyph test (brief §5) — performed for real, not assumed

Test string: `Oʻzbekiston · gʻoya · maʼlumot · Ўзбекистон · Щёлочь · Qq Gg 0O 1lI`
(needs U+02BB `ʻ`, U+02BC `ʼ`, U+00B7 `·`, full Cyrillic incl. U+040E/U+045E `Ўў`,
U+0401/U+0451 `Ёё`).

Method: the cmap of every `@fontsource` woff2 subset (latin, latin-ext, cyrillic,
cyrillic-ext, vietnamese) was parsed with `fonteditor-core`, and each subset's declared
`unicode-range` was checked (the browser only uses a face for characters inside its
`unicode-range`; the Google Fonts `latin` range includes `U+02BB-02BC`).

| Candidate | Result | Verdict |
|---|---|---|
| Bitter | All test glyphs present (ʻʼ in latin, Cyrillic complete) | **chosen — display** |
| Literata | All test glyphs present | **chosen — reading** |
| Golos Text | **U+02BB `ʻ` missing from every subset** — Oʻ/Gʻ would fall back | rejected |
| **Onest** | All test glyphs present; Cyrillic-first design | **chosen — interface** |
| JetBrains Mono | **U+02BB `ʻ` missing from every subset** — Uzbek strings in code would fall back | rejected |
| **Source Code Pro** | All test glyphs present (full Cyrillic since 2023) | **chosen — code** |
| Fira Code, Roboto Mono | U+02BB missing | rejected |

This is why the brief's two recommendations had to be swapped: Golos Text → **Onest**,
JetBrains Mono → **Source Code Pro**. Both replacements are the brief-sanctioned style of
alternative (Onest is named in the brief itself).

### 2.3 Type scale (tokens, 8 steps)

| Token | px / line-height | Tracking | Use |
|---|---|---|---|
| `display-2xl` | 44 / 48 | −2 % | Login university name, marketing-scale heroes |
| `display-xl` | 36 / 40 | −2 % | Page heroes (dashboard greeting) |
| `display-lg` | 28 / 34 | −1.5 % | Page titles (display face) |
| `title` | 22 / 28 | −1 % | Section titles, card headers |
| `lead` | 18 / 30 (17 / 28 on phone) | 0 | Lesson body (reading face), lead paragraphs |
| `body` | 16 / 24 | 0 | Interface body, forms, tables |
| `small` | 14 / 20 | 0 | Secondary text, dense tables, badges |
| `caption` | 12 / 16 | +6 % | Meta, labels, overlines (uppercase) |

Rules: lesson column 65–72 characters (max ≈ 680 px); `text-wrap: balance` on display/title,
`pretty` on paragraphs; tabular figures in tables, timetables, timers, scores; real quotes
and dashes per language (« » uz/ru, “ ” en). Russian is the longest language — layouts are
drawn against it.

---

## 3. Colour tokens

Brand-fixed: `brand-navy #002E60`, `brand-magenta #FF0158`. Everything else is semantic and
defined once per theme in `@theme`; components never use `dark:` for colour and never hold a
hex value. Every ratio below was computed with the WCAG formula (not estimated).

### 3.1 Light theme

| Token | Value | Contrast | Notes |
|---|---|---|---|
| `surface` | `#FBFAF7` | — | warm paper white |
| `surface-raised` | `#FFFFFF` | — | cards, menus |
| `surface-sunken` | `#F4F2EC` | — | wells, code block chrome |
| `text` | `#0F1E33` | 16.1 / 16.8 / — on surface / raised | navy ink |
| `text-muted` | `#47586E` | 7.0 / 7.3 / 6.5 on surface / raised / sunken | cool navy-grey |
| `border` | `#E0E3E9` | hairline | 1 px quiet lines |
| `border-strong` | `#B9C2D0` | — | input borders |
| `primary` | `#002E60` | white on it 13.5 | buttons, key structure |
| `primary-hover` | `#0B3D77` | 10.3 on surface | hover step, links |
| `primary-foreground` | `#FFFFFF` | — | |
| `accent` | `#FF0158` | 3.7 on surface | **fills and large shapes only** (now-markers, today column, rings) |
| `accent-strong` | `#D8004B` | white on it 5.2 | accent buttons |
| `accent-text` | `#C40045` | 5.9 / 6.1 / 5.1 on surface / raised / accent-soft | accent links & small text |
| `accent-soft` | `#FFE4EE` | — | accent tint background |
| `accent-foreground` | `#FFFFFF` | — | |
| `success` | `#0E7A4A` | 5.2 on surface, 4.7 on success-soft | |
| `success-soft` | `#E3F4EB` | — | |
| `warning` | `#9A5B00` | 5.2 on surface, 4.8 on warning-soft | |
| `warning-soft` | `#FBF0DC` | — | |
| `danger` | `#B42318` | 6.3 on surface, 5.5 on danger-soft | true red, clearly not magenta |
| `danger-soft` | `#FBE7E4` | — | |
| `info` | `#1A5FAD` | 6.1 on surface, 5.5 on info-soft | |
| `info-soft` | `#E5EEF9` | — | |
| `focus-ring` | `#D8004B` | — | 2 px, 2 px offset |

### 3.2 Dark theme — deep navy-black built from brand navy

| Token | Value | Contrast | Notes |
|---|---|---|---|
| `surface` | `#081120` | — | navy-black |
| `surface-raised` | `#0E1B30` | — | cards, menus |
| `surface-sunken` | `#040A14` | — | wells |
| `text` | `#E9EEF6` | 16.2 / 14.8 on surface / raised | |
| `text-muted` | `#9AA9C0` | 7.9 / 7.2 / 8.3 on surface / raised / sunken | |
| `border` | `#1C2C46` | hairline | |
| `border-strong` | `#33486B` | — | input borders |
| `primary` | `#6B9BD8` | `primary-foreground` on it 6.6 | buttons flip: light fill, dark text |
| `primary-foreground` | `#081120` | — | |
| `primary-hover` | `#8FB4E4` | 8.8 on surface | links |
| `accent` | `#FF0158` | fills only, unchanged | |
| `accent-strong` | `#D8004B` | white on it 5.2 | accent buttons (same as light) |
| `accent-text` | `#FF5C93` | 6.5 / 5.9 / 5.7 on surface / raised / accent tint | |
| `accent-soft` | `#3A1020` | — | |
| `accent-foreground` | `#FFFFFF` | — | |
| `success` | `#3FBF7F` | 8.1 on surface | |
| `warning` | `#E5A13D` | 8.6 on surface | |
| `danger` | `#F26D7D` | 6.5 on surface | |
| `info` | `#6EA8E8` | 7.6 on surface | |
| `focus-ring` | `#FF5C93` | — | |

Dark status tints, derived like the dark session tints (hue over `surface-raised`) and verified
with the same WCAG computation: `success-soft #0E3036` (6.0 : 1 with `success`),
`warning-soft #302E2C` (6.1 : 1), `danger-soft #3C232F` (4.9 : 1), `info-soft #112D51`
(5.6 : 1), `accent-soft #3A1020` (5.7 : 1 with `accent-text`).

### 3.3 Session types — six colour-blind-safe hues (Okabe–Ito based), none of them magenta

Same in timetable, course plan, badges, legend. Colour never works alone: always a label or
icon. `LECTURE · SEMINAR · PRACTICAL · LABORATORY · INDEPENDENT_STUDY · COURSE_WORK`.

| Type | Light tint | Light strong | ratio on tint / white | Dark tint | Dark strong | ratio on tint |
|---|---|---|---|---|---|---|
| LECTURE | `#E7F0FA` | `#00689F` | 5.2 / 6.0 | `#0B2E4D` | `#7FB8EA` | 6.6 |
| SEMINAR | `#E4F3EF` | `#00755A` | 5.0 / 5.7 | `#0B383F` | `#4FCEA5` | 6.5 |
| PRACTICAL | `#FBF0DA` | `#8A5A00` | 5.2 / 5.9 | `#3E3825` | `#F0BE62` | 6.8 |
| LABORATORY | `#FBE9E0` | `#A84300` | 5.1 / 6.1 | `#3E2B24` | `#F09468` | 5.8 |
| INDEPENDENT_STUDY | `#E9F5FB` | `#386F8F` | 4.9 / 5.5 | `#1C3A55` | `#8FCDEE` | 6.8 |
| COURSE_WORK | `#F9E9F1` | `#8F4A77` | 5.3 / 6.2 | `#38304A` | `#DCA3C4` | 6.0 |

Distinguishability notes: two blues (LECTURE vs INDEPENDENT_STUDY) differ in lightness and
are never adjacent without labels; PRACTICAL (amber) vs LABORATORY (vermillion) differ in
hue and both are far from `danger` red and from magenta; COURSE_WORK's mauve is muted where
the accent magenta is vivid — and the accent never paints a session badge.

### 3.4 Verdict colours

`ACCEPTED` → success · `PARTIAL` → warning · `WRONG_ANSWER`, `RUNTIME_ERROR`,
`TIME_LIMIT_EXCEEDED`, `MEMORY_LIMIT_EXCEEDED`, `COMPILATION_ERROR`, `OUTPUT_LIMIT_EXCEEDED`
→ danger family, told apart by icon and label (never colour alone) · `QUEUED` / `RUNNING` →
info · platform `FAILED` → danger. Charts: navy series by default, accent for the
highlighted one; never rainbow.

---

## 4. Space, shape, depth, motion

- **Spacing** (4 px base): 4 · 8 · 12 · 16 · 20 · 24 · 32 · 40 · 48 · 64 · 80 · 96.
  Gutters 16 (390) / 24 (768) / 32–48 (1440). Content max 1280 px; lesson column ≈ 680 px.
- **Radii**: `sm` 6 (controls) · `md` 10 · `lg` 12 (cards) · `full`. Concentric: inner
  radius = outer − padding.
- **Shadows** — rare, soft, navy-tinted, floating things only (never with a border):
  `menu` 0 4 16 −2 / rgb(7 18 36 / .12) + 0 2 6 / .08 · `dialog` 0 16 48 −12 / .25 ·
  `drag` 0 12 32 −8 / .30.
- **Durations**: 120 · 200 · 320 · 600 ms.
- **Easings**: `standard` cubic-bezier(.25,.46,.45,.94) · `emphasized`
  cubic-bezier(.16,1,.3,1) (entrances) · `spring` cubic-bezier(.34,1.56,.64,1) (drag settle).
- **Z-index**: base 0 · sticky 10 · header 20 · drawer 30 · overlay 40 · dialog 50 ·
  toast 60 · tooltip 70.
- Hit areas ≥ 44 px on touch. Focus ring 2 px `focus-ring`, 2 px offset, always visible.
- Icons: Lucide, one stroke weight, no emoji anywhere.

---

## 5. App shell at 390 / 768 / 1440

**1440 (≥ 1024 px — desktop).** Left rail, 72 px collapsed / 264 px expanded, state
remembered per user: emblem at top (the redrawn SVG shield, 40 px), the role's sections as
icon + label items, the role switch (for accounts like n.ayitbayev) above the profile area at
the bottom. Slim top bar (64 px): page title or breadcrumb left; right side — search trigger
("Ctrl+K"), language switch UZ / RU / EN (always visible, spec G-01), theme switch,
notification bell with unread count, profile menu. Content on the 12-column grid, max 1280,
gutter 48.

**768 (tablet).** Rail always collapsed to 72 px (icons with tooltips). Top bar as desktop,
gutter 24. Two-column grids collapse to one column where content needs the width.

**390 (phone).** No rail. Top bar 56 px: emblem + page title, bell, profile. Bottom tab bar
with the role's four or five main sections; everything else under "More" (a sheet). Gutter
16. Tables become stacked rows or scroll inside their own container — the page never scrolls
sideways.

**Focus modes.** The lesson page, the practice-task page and presentation mode hide the
shell chrome and give the work the whole screen (brief §7).

**Page anatomy.** Header: title in Bitter `display-lg`, one line of context in `text-muted`,
the one primary action at right. Then content. Four states (skeleton shaped like the
content / empty with a next action / error with retry + request id / content) designed
together for every screen.

---

## 6. The shield motif — where it is used (and nowhere else)

1. **Login brand pane**: the campus photo sits under a navy duotone, its lower edge masked
   by the shield silhouette — the brand pane's bottom literally takes the shield's round
   base and two-tone split.
2. **Avatar frame**: user avatars and the emblem in the profile menu wear the shield
   outline (clip-path), square shoulders and round base.
3. **Emblem itself**: redrawn as SVG (exact proportions, never recoloured) — rail top,
   login, favicon, print header.
4. **One two-tone divider**: the navy/magenta horizontal split of the shield appears as a
   single thin brand rule — on the login page and on system pages (404, no access). Never
   as a repeating pattern.

Later phases may add course emblems in the shield shape; anything beyond this list goes
through this file first.

---

## 7. The login page (the public face)

**Desktop ≥ 1024** — two panes, 45 / 55:

- **Brand pane** (navy field): the campus photograph large under a navy duotone (it is
  low-resolution; the treatment is mandatory), bottom edge masked by the shield silhouette;
  the SVG emblem top-left; below it the university name in Bitter `display-lg`, white, two
  lines: «Aniq va Ijtimoiy Fanlar Universiteti»; at the very bottom the thin navy/magenta
  two-tone rule. No slogans, no statistics — nothing invented.
- **Form pane** (paper `surface`): top-right the language switch (UZ / RU / EN segmented
  control). Centre-left, max 400 px: small emblem, «Kirish» in Bitter `display-lg`; one
  line of context; username input; password input with show/hide eye; the error line tied to
  the server error code (`AUTH_INVALID_CREDENTIALS`, `AUTH_ACCOUNT_NOT_ACTIVE`) under the
  fields; submit button full-width, `accent-strong` — the single accent of the page.
  Footer: short university name + year.

**390 / 768** — one column: the brand collapses to a compact header band (duotone strip,
emblem 40 px, name in Bitter `title`), the form below with gutter 16; language switch in
the header band.

**Motion** (brief §8, moment 1): on first paint the shield's two halves meet, the name sets
in, the form follows — once, then never again in the session; a simple fade under
`prefers-reduced-motion`.

**Shared language.** Forced password change (`mustChangePassword`), session-expired and
"no access" pages reuse the same two-pane/compact composition so the public surface feels
like one publication. (No "forgot password" link: the spec gives password reset to admins —
listed in BACKEND-REQUESTS.md only if the owner wants self-service reset.)

---

## 8. Names and translations to confirm

| Locale | University name |
|---|---|
| uz | Aniq va Ijtimoiy Fanlar Universiteti (official, fixed) |
| ru | Университет точных и социальных наук *(from the brief — please confirm)* |
| en | University of Exact and Social Sciences *(from the brief — please confirm)* |

## 9. Asset handling

`academic-materials/branding/AIFU_logo.png` → copied to `src/assets/brand/` and redrawn as
`aifu-shield.svg` (exact proportions: square shoulders, round base, `AI` over `FU`,
EST 2022). `photo_2026-09-23_16-57-59.jpg` → `src/assets/brand/campus.jpg`, used only under
the navy duotone. No other imagery anywhere: no stock, no generated people or campuses.

---

*Decisions the brief left open and that were made here: the full neutral/status palette
steps, the dark-theme primary flip (light fill + dark text), the session-type hue mapping,
the type scale steps, the three easings, the rail widths, the shield usage list, the login
pane split. Everything else follows the brief.*

---

## 10. Changes made while building F0 (after looking at the screens)

- **Emblem geometry.** The first SVG was squatter than the logo. It is now drawn from
  measurements of `AIFU_logo.png`: 76 wide, 93 tall, straight sides for 55 and a semicircle of
  radius 38, navy for the top 41, letters at the measured cap lines. A white keyline surrounds
  it, so the navy half stays readable on a dark surface (dark theme, the login pane).
- **Login brand pane.** The photograph in a small shield on navy came out muddy. The pane itself
  is now the emblem at large: navy above a magenta foot. The photograph fills the upper 54 % of
  the pane in the shield's silhouette (square shoulders, round base) under a navy *screen*
  duotone — darks become navy, lights stay light — with the emblem, the name in `display-xl`
  and the tagline beneath. On a phone the brand band ends in a magenta line.
- **Avatar frame.** Square shoulders and a round base by border radius (`rounded-b-full`), not
  by a polygon clip path: smoother, and no inline style.
- **Two-tone rule** stays on light surfaces (system pages). On navy its navy half is invisible,
  so the login pane uses a plain magenta foot.
- **Type-scale classes and colours.** `tailwind-merge` took `text-body` for a colour and dropped
  the real text colour next to it (white button labels turned dark). `cn()` and `tv()` are now
  the project's own (`src/shared/lib`), configured with the type scale and the shadow tokens.
- **Password rule** on the change-password form follows the server: 10 to 72 characters.
