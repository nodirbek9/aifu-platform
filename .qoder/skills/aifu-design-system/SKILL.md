---
name: aifu-design-system
description: 'The AIFU platform design system — brand colours, the shield motif, typography roles, tokens, session-type and verdict colours, layout shell, motion moments and the list of things not to do. Use whenever you create or restyle any screen or component in frontend/. Triggers on: design, UI, style, theme, colour, color, font, layout, component, page, screen, dark mode, token, brand, logo, beautiful, polish.'
---

# AIFU design system

The full brief is `docs/04-DESIGN-BRIEF.md`. Read it once per session before designing. This
skill is the working checklist.

## The bar

Looks like the platform of a major university (MIT OCW, Harvard, Stanford Online, ETH, edX):
typography-led, restrained, exact, finished in every state. Not a template admin panel. The
default look of shadcn/ui is a starting skeleton, never the result.

## Fixed values

- `brand-navy` `#002E60` — structure. `brand-magenta` `#FF0158` — the single accent.
- Magenta means "now / the next action": primary button, current class, today, current step.
  Count the magenta on a screen; more than a few places is wrong.
- Pure magenta is 3.9 : 1 on white: fills and large type only. Body-size text and white-on-
  magenta buttons use the darker step (about `#D8004B`). Verify every text pair ≥ 4.5 : 1.
- Type roles: display = slab serif (Bitter) · reading = text serif (Literata / Source Serif 4) ·
  interface = Cyrillic-first sans (Golos Text / Onest) · code = JetBrains Mono, ligatures off.
  Self-hosted via `@fontsource`. Test string, no fallback glyphs allowed:
  `Oʻzbekiston · gʻoya · maʼlumot · Ўзбекистон · Щёлочь · Qq Gg 0O 1lI`.
- Emblem: `academic-materials/branding/AIFU_logo.png` (copy to `src/assets/brand/`, redraw as
  SVG). Campus photo: `academic-materials/branding/photo_2026-09-23_16-57-59.jpg`, only under a
  navy duotone treatment. No other imagery; no stock or generated people.

## Tokens first

1. Everything visual is a token in `src/shared/ui/tokens.css` (`@theme`): colour (semantic:
   `surface`, `surface-raised`, `text`, `text-muted`, `border`, `primary`, `accent`, `success`,
   `warning`, `danger`, `info`), six session-type colour sets, verdict colours, type scale,
   spacing, radii, shadows, durations, easings, z-index.
2. Light and dark are two sets of the same semantic tokens. Components never use `dark:`
   variants for colour and never contain a hex value or an arbitrary `[…]` value.
3. A new visual need = a new token or a new variant of a `shared/ui` component, not a one-off
   class in a feature.

## Session types (same everywhere: timetable, course plan, badges, legend)

`LECTURE` · `SEMINAR` · `PRACTICAL` · `LABORATORY` · `INDEPENDENT_STUDY` · `COURSE_WORK` — six
colour-blind-safe hues, each with tint / strong / dark-theme values, none of them magenta, always
with a label or an icon.

## Building a screen

1. Name the one primary action and the one thing the user came for. They get the visual weight.
2. Lay out at 390 px first, then 768, then 1440. Use the longest language (Russian is about 30 %
   longer than English). Nothing truncates in navigation or buttons.
3. Hierarchy from type and space. Reach for a border before a shadow, and for space before a
   border. No cards inside cards.
4. Design the four states together: skeleton shaped like the content, empty state with a next
   action, error state with retry and request id, content.
5. Tables: sticky header, tabular figures, right-aligned numbers, row hover; on a phone stacked
   rows or an inner scroll area — the page never scrolls sideways.
6. Motion: only where it explains a change. Use the duration and easing tokens. `transform` and
   `opacity` only. Honour `prefers-reduced-motion`. The eight choreographed moments are listed in
   the brief §8 — the verdict reveal and the timetable are the ones to make memorable.
7. Add the component to `/dev/ui` if it is new, in all its states, both themes.
8. Write the decision down in `frontend/DESIGN-DECISIONS.md` if you chose something the brief
   left open.

## Never

Purple or violet gradients · glassmorphism · neon glow · emoji as icons · waving-hand greetings ·
stock or generated illustrations · invented statistics or slogans · rainbow charts · spinner
where a skeleton belongs · layout shift when data arrives · centre-aligned everything · shadow
plus border on the same element · animation on every element · lorem ipsum or sample data.

## Self-review before you call a screen done

Take screenshots (see `aifu-screen-done`) and answer honestly:

- Could this screen be from any SaaS template? If yes, it is not done.
- Is the hierarchy obvious in two seconds with the text blurred?
- Does everything sit on the grid? Are the spacings from the scale?
- Is magenta used only for "now / next action"?
- Dark theme: designed, or merely inverted?
- Russian strings: anything clipped, wrapped badly, or overflowing?
