# Skills for the frontend agent

Project skills live in `.qoder/skills/<name>/SKILL.md`, where Qoder picks them up. An agent loads a skill
when the task matches its description, or on demand with `/<name>`.

## Written for this project

| Skill | Use it for |
|---|---|
| `aifu-design-system` | Any screen or component: brand, tokens, typography, layout, motion, what not to do |
| `aifu-api` | Any server data: typed client, auth and refresh, errors, locale, polling, files |
| `aifu-screen-done` | Before calling anything finished: gates, browser check, screenshots, report |

## General skills (third party)

From [hzblj/skills](https://github.com/hzblj/skills) by Jan Blazej, MIT licence
(`LICENSE-hzblj-skills.txt`), copied unchanged:

| Skill | Use it for |
|---|---|
| `tailwind` | Tokens in `@theme`, light / dark by flipping tokens, `tv()` variants, class order |
| `type-safety` | Strict TypeScript: no `any`, no casts, discriminated unions, validation at the edge |
| `compound-components` | Flexible components made of named parts (tabs, dialogs, the data grid) |
| `ui-typography` | Text wrapping, tabular figures, font smoothing, no layout shift |
| `ui-surfaces` | Concentric radii, optical alignment, shadows, hit areas |
| `ui-interactions` | Press, hover, focus, disabled and loading states |
| `ui-motion` | Interruptible transitions, staggered enter, quiet exit, icon cross-fade |
| `css-animations` | Motion in plain CSS, reduced motion |
| `gsap-react` | GSAP in React with `useGSAP`: scope, cleanup, `contextSafe` |
| `gsap-timelines` | Sequences: the verdict reveal, the login intro |
| `gsap-scroll-trigger` | Scroll-linked motion (reading progress, long pages) |
| `gsap-text` | Headline reveals with SplitText |
| `gsap-performance` | Only `transform` and `opacity`, reduced motion, no jank |

These skills also mention React Native and Next.js; this project is a React + Vite single-page
app for the web, so only the web parts apply. Where a general skill and `AGENTS.md` or
`docs/04-DESIGN-BRIEF.md` disagree, the project files win.

Two more skills are useful and are installed in Qoder at user level, not in this folder:
`frontend-design` (commit to a distinctive design direction) and `web-design-guidelines`
(review a UI against the Web Interface Guidelines).
