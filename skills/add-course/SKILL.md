---
name: add-course
description: Add a new subject or a new course delivery (all lessons, test banks, practice, midterm) to the platform with one chain of tools and no code change. Use when asked to "add a subject/course", "load a syllabus", or "write the tests for a course".
---
# Adding a subject or a course

A new subject is **data**, never code (CLAUDE.md §11, `docs/academic-analysis/HOW-TO-ONBOARD-A-SYLLABUS.md`).
Nothing in `backend/`, `frontend/` or `e2e/` names a course: if you find yourself editing them for
a new course, stop — something is missing from the tools; fix the tool and say so.

## The chain

```
content/<COURSE>/course.json     the manifest: subject, term, scheme, modules, lessons   (docs/06 §1a)
content/<COURSE>/<LESSON>/       uz.md ru.md en.md examples.json                         (docs/06 §2–§4)
content/<COURSE>/<LESSON>/assess.json   test bank + practice                             (docs/06 §7a)
content/<COURSE>/midterm.json    60 questions up to the anchor lesson                    (docs/06 §7a)
```

```bash
export AIFU_PASSWORD=…          # AIFU_API / AIFU_USER default to http://localhost:8080 / admin
node tools/content/onboard.mjs <COURSE> --dry-run          # plan, writes nothing
node tools/content/onboard.mjs <COURSE> --author           # + an AI worker writes the banks and the midterm
node tools/content/onboard.mjs <COURSE> --enable           # + switch the learning path on
```

`onboard.mjs` runs, in order and idempotently: scaffold (subject, course, scheme, modules, lessons; lesson titles of the manifest only fill gaps — the lesson texts own them) →
lesson texts → authoring (`--author`) → check → verify (Docker) → import → learning-path config.
It stops at the first failing step; fix and run the same command again.

A new subject and course are created **DRAFT**: students see nothing until an administrator publishes them
through the platform's publication workflow (the validators check translations, weights and required
lessons). The chain never publishes academic data on its own. `--dry-run` on a new course shows the
scaffold plan, parses the lessons and checks the banks; the import/path steps need the course to exist.
Proven end to end on a throwaway course (scaffold → lessons → check → verify → import → path, a rerun
writes nothing, then soft-deleted). The platform upper-cases assessment component codes (`YaN` → `YAN`).

## Rules

1. **Never invent academic data.** What the syllabus does not say stays absent; record the gap in
   `docs/academic-analysis/PENDING-INPUTS.md`. A course without an `ON` (midterm) component gets
   an ungraded midterm that only gates the path.
2. **Lesson texts** follow `docs/06-CONTENT-GUIDE.md` exactly (same skeleton in uz/ru/en; every code
   example runs: `node tools/content/run-examples.mjs <COURSE>`).
3. **Banks** are written from the lesson text only; `assess.mjs check` and `assess-verify.mjs` are the
   judges, not the author. Starters must fail at least one test; solutions pass all tests in
   python, java and cpp; NUM `tolerance` < 1,000,000; uz uses ʻ (U+02BB) and ʼ (U+02BC).
4. **Do not enable the path blindly.** `--enable` locks lessons for students who did not finish the
   previous one (ADR-015/018). Ask the owner unless they already said to enable.
5. **Seeded fixtures:** the dev seed owns a few activities that end-to-end tests use (DAS1110-S1
   `M2`, `A2`, `A3`, `A8`, `MT6`); the importer skips occupied steps unless `--replace` is given —
   never use `--replace` on them.
6. After a run: `node --test tools/content/*.test.mjs` is green, and the plan screen of the course
   (student view) shows the steps. Report what was written, what failed, and the numbers
   (lessons, TEST, PRACTICE, midterm questions).

## Writing with AI workers

`assess-author.mjs` calls the owner's model router (`AI_ROUTER`, role `docs`, no pinned model). Any other
agent can do the same job by following the prompts in that file; the checker is the contract. Batches of
1–3 lessons per worker finish in minutes; workers sometimes stop without writing — the tool detects the
missing file and retries once. If every worker is out of quota, say so and wait; do not hand-write 200
lessons on the main model.
