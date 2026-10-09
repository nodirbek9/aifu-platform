# 06 — Content guide

How the lessons of the platform are written, in Uzbek, Russian and English, and how they get
into the platform. The content lives in the repository as text files, is reviewed like code, and
is imported through the API by `tools/content/import.mjs` — into the development database or a
production one alike. Nothing here is a database seed.

## 1. Where it lives

```
content/
  DAS1110-S1/                 one folder per course delivery
    course.json               which course: {"subjectCode": "DAS1110", "termName": "1-semestr"}
    M1/                       one folder per lesson, named by the lesson's code
      uz.md  ru.md  en.md     the same lesson in each language
      examples.json           the input and expected output of each code example (§6)
```

The three files of a lesson have **the same skeleton**: the same blocks in the same order. The
importer refuses a lesson whose languages disagree, so a translation can never silently drop a
section.

## 1a. Create the course skeleton from `course.json`

`tools/content/scaffold.mjs` creates the subject, its delivery in an **existing** term, the
assessment tree, modules and lessons through the admin/teacher API. No database access or
backend edits are needed. Then the existing importer loads the lesson texts by code:

```bash
AIFU_API=http://localhost:8080 AIFU_USER=admin AIFU_PASSWORD=… node tools/content/scaffold.mjs content/<COURSE> --dry-run
AIFU_API=http://localhost:8080 AIFU_USER=admin AIFU_PASSWORD=… node tools/content/scaffold.mjs content/<COURSE>
AIFU_API=http://localhost:8080 AIFU_USER=admin AIFU_PASSWORD=… node tools/content/import.mjs content/<COURSE>
```

The argument may also be the path to `course.json`. Credentials are required for dry-run:
after the shared client's authentication POST, **all scaffold calls are GETs**. Dry-run
prints every proposed HTTP write, with symbolic IDs for entities that do not yet exist, and
writes no academic data. Use an admin account; a teacher alone cannot create catalogue data.

Complete example below is a **synthetic format/test fixture, not an AIFU syllabus**. Replace
its academic values and translations with reviewed source material, and `TEST` with the code
of an existing department. Do not populate unknowns from this example or another subject.
Follow [the syllabus onboarding rules](academic-analysis/HOW-TO-ONBOARD-A-SYLLABUS.md).

```json
{
  "subjectCode": "TEST1000",
  "termName": "1-semestr",
  "academicYearCode": "2026-2027",
  "title": "Sinov kursi",
  "subject": {
    "names": {
      "uz": "Sinov fani",
      "ru": "Тестовая дисциплина",
      "en": "Test subject"
    },
    "departmentCode": "TEST",
    "type": "ELECTIVE",
    "defaultInstructionLanguage": "uz"
  },
  "course": {
    "credits": 1,
    "totalHours": 4,
    "instructionLanguage": "uz",
    "gradeScaleCode": "AIFU_100_5",
    "titles": {
      "uz": "Sinov kursi",
      "ru": "Тестовый курс",
      "en": "Test course"
    },
    "sessionAllocations": {
      "LECTURE": 2,
      "PRACTICAL": 2
    }
  },
  "assessmentComponents": [
    {
      "code": "JN",
      "kind": "CONTINUOUS",
      "maxPoints": 30,
      "minPassPercent": 60,
      "blocksFinal": true
    },
    {
      "code": "JN_P",
      "kind": "PRACTICAL_ACTIVITY",
      "maxPoints": 30,
      "parent": "JN"
    },
    {
      "code": "ON",
      "kind": "MIDTERM",
      "maxPoints": 30,
      "minPassPercent": 60,
      "blocksFinal": true
    },
    {
      "code": "YaN",
      "kind": "FINAL",
      "maxPoints": 40,
      "minPassPercent": 60,
      "assessmentForm": "WRITTEN"
    }
  ],
  "modules": [
    {
      "code": "UNIT1",
      "groupingSource": "PLATFORM_INFERRED",
      "titles": {
        "uz": "Sinov moduli",
        "ru": "Тестовый модуль",
        "en": "Test module"
      },
      "lessons": [
        {
          "code": "M1",
          "sessionType": "LECTURE",
          "hours": 2,
          "titles": {
            "uz": "Sinov maʼruzasi",
            "ru": "Тестовая лекция",
            "en": "Test lecture"
          }
        },
        {
          "code": "A1",
          "sessionType": "PRACTICAL",
          "hours": 2,
          "titles": {
            "uz": "Sinov mashgʻuloti",
            "ru": "Тестовое занятие",
            "en": "Test practical"
          }
        }
      ]
    }
  ]
}
```

| Field | Rule |
|---|---|
| `subjectCode`, `termName`, `title` | Existing importer fields keep working. Code and term name are required; title is optional. A legacy manifest without creation sections only locates an existing course; it never invents its skeleton. |
| `academicYearCode` | Optional disambiguator. Term lookup searches all years by exact name. Missing/ambiguous terms fail before writes and list available names and years; no term is created. |
| `subject.names`, `departmentCode` | Required if `subject` is supplied. Names have `uz`, `ru`, `en`; department is looked up by exact code. |
| `subject.type`, `defaultInstructionLanguage` | Optional. Type is `MANDATORY`, `ELECTIVE` or `OPTIONAL`; instruction language is `uz`, `ru` or `en`. Omitted fields are not sent; the platform applies its own defaults. |
| `course.credits`, `totalHours` | Required if `course` is supplied. Credits: 0.1–60, one decimal place; total hours: positive integer. Backend requires total hours even though OpenAPI omits it from its `required` list. No credit-to-hours conversion is assumed. |
| `course.cohortCode` | Optional; course identity is subject + term + cohort (platform default `DEFAULT`). Legacy lookups with several deliveries fail as ambiguous. |
| `course.titles` | Optional three-language titles. Without it, a supplied legacy `title` writes only the Uzbek course title when the `course` section is present. When both are supplied, `title` must equal `course.titles.uz`. |
| `course.instructionLanguage`, `gradeScaleCode` | Optional creation fields. Omitted grade scale uses the platform default; the syllabus onboarding guide specifies when to reuse `AIFU_100_5`. |
| `course.sessionAllocations` | Optional object keyed by session type, with non-negative integer hours. If supplied, its hours must sum to `totalHours`. Omit unspecified session types rather than filling them with zero. |
| `assessmentComponents` | Optional complete rubric. Codes are unique, case-insensitively. Root `maxPoints` sum to 100; each group of children sums to its parent's `maxPoints`. `parent` references another component code; omit it on roots. Unknown parents and cycles fail offline. Parents are created before children, even if listed later. Display order follows array order. |
| Component fields | Required: `code`, `kind`, positive `maxPoints`. Optional: `minPassPercent` (0–100), `blocksFinal`, `assessmentForm`. Kinds: `CONTINUOUS`, `MIDTERM`, `FINAL`, `ATTENDANCE`, `INDEPENDENT_WORK`, `PRACTICAL_ACTIVITY`, `PROJECT`, `COURSE_WORK`. Forms: `WRITTEN`, `ORAL`, `MIXED`, `PRACTICAL`, `TEST`, `PROJECT`. Points and percentages allow two decimals. No pass gate is inferred. |
| `modules[].code`, `titles`, `lessons` | Required per supplied module. Array order is the teaching order. `groupingSource` is optional: `SYLLABUS_EXPLICIT`, `PLATFORM_INFERRED`, `TEACHER_DEFINED`; the platform defaults to inferred grouping. Use explicit grouping only when the syllabus supplies it. |
| `lessons[].code`, `sessionType`, `titles` | Required per lesson. Lesson codes are unique across the course and match the Markdown folder names. Types: `LECTURE`, `SEMINAR`, `PRACTICAL`, `LABORATORY`, `INDEPENDENT_STUDY`. |
| `lessons[].hours` | Optional, non-negative, up to 999.99 (database precision). If all lesson hours for a session type are known, their sum must equal its declared budget. Unknown hours remain absent; no estimates are inserted. Every supplied lesson type needs a budget when `sessionAllocations` is supplied. |

All manifest errors, including weight and hour errors, are reported **before any API call**.
The tool then reads every page of existing subjects/courses/modules/lessons and plans writes.
An unchanged rerun performs no academic writes, including translation, ordering and status
updates. Missing pieces of an interrupted run can be filled by rerunning the same manifest.
Changed titles/hours are updated while preserving existing descriptions, lesson summaries and
lesson metadata. Existing modules/lessons outside the manifest are retained after its ordered
entries; an existing lesson in a different module is a conflict, not a silent move.

Lessons are created `DRAFT`; the separate PATCH sets known hours and PUTs set translations.
Only after the entire skeleton is complete are lessons published in manifest order, followed
by modules. The **subject and course remain DRAFT when newly created**: review lesson content,
activities and the syllabus, then use the platform publication workflow. This command does
not create outcomes, prerequisites, teachers, activities or content blocks.

Platform limitations: there is no transaction across these HTTP calls, so an API failure can
leave a resumable partial skeleton. Archived modules/lessons must be restored explicitly.
The API has no component edit endpoint and no credits/total-hours update endpoint; conflicting
catalogue fields or rubric nodes are reported before writes. An extra existing rubric node
also fails instead of deleting or guessing weights. Grade scale is a creation-only input and
is not exposed in `CourseDetail`, so its existing value cannot be checked on a rerun. Run one
scaffold at a time; concurrent creators have no scaffold-wide idempotency key.

## 2. What a lesson contains

The order is fixed; every lesson follows it, so a student always knows where they are.

1. **Why it matters** — a real situation the topic solves (a group journal, a shop's prices, a
   university's timetable), 1–2 paragraphs. No history lessons.
2. **Objectives** — a `:::note` with 3–5 things the student will be able to *do* afterwards.
3. **The theory** — one `##` section per idea, each short: definition in plain words, the rule,
   a small example, a table where a comparison is clearer than prose. Terms in **bold** the
   first time; the Uzbek term first, then in brackets the English one where students meet it in
   documentation (`massiv (array)`).
4. **Worked examples** — code in **Python, Java and C++** (three fences one after another become
   one example with language tabs), each followed by a step-by-step explanation of what the
   program does and what it prints. Programs are complete and runnable; input comes from
   standard input, output goes to standard output — the same contract as the practice tasks.
   Comments in the code are in the language of the file.
5. **Common mistakes** — a `:::warning` with the errors students really make, and why.
6. **Tips** — a `:::tip` (optional).
7. **Summary** — 4–7 key points.
8. **Self-check** — a `:::check` block of 4–6 questions with their answers (shown on demand).
9. **Further reading** — 1–3 `:::link` blocks to reliable sources (cppreference,
   docs.python.org, docs.oracle.com, visualgo, the course textbook).

Practice (the coding task, the quiz, an assignment) is not written here: those blocks already
belong to the lesson and the importer keeps them, after the content.

Length (prose, without the code): a lecture 1 000–2 000 words, a practical lesson 800–1 500 (it
leans on worked examples), an independent-study topic 600–1 200 including the task statement. Level: first-year
students, no prior programming assumed in the first module.

## 3. Language

- **Uzbek** — Latin script, the official orthography: `oʻ`, `gʻ` with ʻ (U+02BB), the apostrophe
  ʼ (U+02BC) in `maʼlumot`, `taʼlim`. Established terms: dastur, oʻzgaruvchi, massiv, sikl,
  shart, funksiya, satr, koʻrsatkich, fayl, kompilyator, interpretator.
- **Russian** — standard academic Russian: программа, переменная, массив, цикл, условие,
  функция, строка, указатель.
- **English** — plain international English.
- The same facts, examples and numbers in every language: a translation, not a rewrite.

## 4. Markup

Prose is Markdown. Every `##` heading starts a new text section (and an entry of the table of
contents). Special blocks:

````markdown
```python            three fences in a row = one example with tabs
print("Salom")
```
```java
...
```
```cpp
...
```

```text              pseudocode or a sample of output: shown as is, not a runnable example
agar ball >= 56:
    oʻtdi
```

:::note              a callout: note | tip | warning | danger
Text in Markdown.
:::

:::case              a case study (a framed real situation)
...
:::

:::check             self-check: Q: and A: lines, as many pairs as needed
Q: Massivning birinchi elementi qaysi indeksda?
A: 0 indeksda.
:::

:::link https://en.cppreference.com/w/cpp/language/array
Label of the link
An optional note on the next line.
:::
````

Front matter, at the top of each file:

```markdown
---
title: Bir oʻlchovli massivlar
summary: Massiv nima, indeks va uzunlik, elementlarni aylanib chiqish, qidirish.
---
```

## 5. Import

```bash
cd tools/content && npm ci
AIFU_API=http://localhost:8080 AIFU_USER=admin AIFU_PASSWORD=… node import.mjs ../../content/DAS1110-S1
node import.mjs ../../content/DAS1110-S1 --dry-run     # parse and check, write nothing
node import.mjs ../../content/DAS1110-S1 --lesson M5   # one lesson
```

For each lesson the importer: checks the three files agree; sets the lesson's title and summary
in each language; removes the lesson's previous text, callout, code, case and link blocks;
writes the new blocks with their three translations; and orders them before the blocks it kept
(the lesson plan, practice references, files). Running it again gives the same lesson: it is
safe to re-import after an edit.

## 6. Review before import

- `node import.mjs <course> --dry-run` passes (the skeletons agree, the Markdown is valid).
- Every code example runs and prints what the text says it prints. `examples.json` lists, for
  each example in order, the inputs and the exact expected output (or `{"skip": "why"}`);
  `node run-examples.mjs ../../content/DAS1110-S1 [--lesson M5]` runs the Python, Java and C++
  versions in the sandbox's own images (Docker) and compares.
- A teacher of the subject reads the Uzbek file; the other languages follow it.

## 7. Practice texts in three languages

The texts of a course's practice — module titles, coding tasks and their hints, quizzes with
their questions and options, course activities — live next to `course.json` as
`practice.uz.json`, `practice.ru.json` and `practice.en.json`, keyed by stable codes (module, task
and quiz codes; question, option and hint positions).

```bash
node practice.mjs export ../../content/DAS1110-S1          # practice.uz.json from the platform
node practice.mjs import ../../content/DAS1110-S1 --dry-run
node practice.mjs import ../../content/DAS1110-S1          # writes the three languages
```

The Russian and English files must have the shape of the Uzbek one (the same keys and the same
number of questions, options and hints). Output that the tests compare literally (`Kabisa`,
`Tub emas`, `Salom, …!`) stays as it is in every language; the statement explains it.

## 7a. Test banks and practice per lesson (`assess.json`, phase 6)

Each lesson folder holds `assess.json`: `test` (8–10 questions, 2+ per difficulty, all three
languages) and `practice` (`kind` `CODE` with a starter and a reference solution in python, java
and cpp and 3+ tests, 2+ hidden; or `ANSWER` with checked questions). A course may have
`midterm.json` (60+ questions, `afterLesson`). The exact format and every rule is
`tools/content/assess-schema.mjs`.

```bash
node tools/content/assess.mjs check <COURSE> [--lesson X]        # offline: format and rules
node tools/content/assess-verify.mjs <COURSE> [--lesson X]       # Docker: solutions pass, starters fail
AIFU_PASSWORD=… node tools/content/assess-import.mjs <COURSE> [--lesson X] [--dry-run] [--replace]
```

Import is idempotent (codes `<COURSE>-<LESSON>-T`, `-P`, `-ON`). A lesson may have one active TEST
and one active PRACTICE (partial unique index): where the dev seed already set one, import skips it
unless `--replace` archives the seeded activity (an archived activity cannot be published again;
go through DRAFT first). The seeded activities of DAS1110-S1 `M2`, `A2`, `A3`, `A8` and `MT6` are
end-to-end fixtures and were kept. For `M2`, `A8` and `MT6` the new bank was imported without an activity (the
import skips the occupied step); for `A2` and `A3` it was imported with an archived activity. A NUM
`tolerance` must stay below 1,000,000 (the column is numeric(12,6)).

## 8. Subject texts in three languages

The subject's own copy — title, short title, aim ("Fan maqsadi"), objectives ("Fanning
vazifasi") and the learning outcomes (TN1, TN2, …) — lives next to `course.json` as
`subject.uz.json`, `subject.ru.json` and `subject.en.json`; learning outcomes are keyed by their
codes. A subject taught in several terms keeps these files in one course folder only (DAS1110
keeps them in `DAS1110-S1`).

```bash
node subject.mjs export ../../content/KIA1406             # subject.uz.json from the platform
node subject.mjs import ../../content/KIA1406 --dry-run
node subject.mjs import ../../content/KIA1406             # writes the three languages
```

The Russian and English files must list the same outcome codes as the Uzbek one.
