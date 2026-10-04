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
```

The three files of a lesson have **the same skeleton**: the same blocks in the same order. The
importer refuses a lesson whose languages disagree, so a translation can never silently drop a
section.

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

Length: a lecture 1 500–2 500 words, a practical lesson 1 000–1 800 (it leans on worked
examples), an independent-study topic 800–1 200 plus the task statement. Level: first-year
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
- Every code example runs and prints what the text says it prints (the examples are short
  enough to check in the platform's own runner: the "Try it" button of the lesson).
- A teacher of the subject reads the Uzbek file; the other languages follow it.
