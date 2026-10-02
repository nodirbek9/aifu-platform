# ADR-005 — Multilingual content: per-entity translation tables

**Status:** Accepted

## Context
Uzbek, Russian, English (brief §13). Localized: subject/course/module/lesson titles &
descriptions, lesson content, quiz questions & options, coding task descriptions,
explanations, hints, learning outcomes. **All supplied material is Uzbek only today**, so the
design must tolerate partial translation without shipping empty strings.

## Options

**A. Localized columns** (`title_uz`, `title_ru`, `title_en`).
Simple joins, trivial queries. But adding Karakalpak or Kazakh = ALTER TABLE on every table;
"required locales" is unenforceable; NULL-vs-missing is ambiguous; wide sparse rows.
Rejected — the brief asks for future expansion.

**B. One generic translation table** (`entity_type`, `entity_id`, `field`, `locale`, `value`).
Zero schema churn. But no foreign keys (orphans guaranteed), no type safety, no per-field
constraints, and every read becomes a pivot of N rows. Queries and Hibernate mapping both get
ugly. Rejected.

**C. JSONB per row** (`title jsonb = {"uz": "...", "ru": "..."}`).
Flexible and compact. But no FK-level integrity, weaker validation, awkward `ORDER BY` on a
localized title, and per-locale full-text indexing needs expression indexes per locale.
Rejected for *short scalar* fields — **accepted for content payloads**, see below.

**D. Per-entity translation table** (`lesson_translations(lesson_id, locale, title, summary)`,
unique on `(lesson_id, locale)`).
Real FKs with `ON DELETE CASCADE`, per-column NOT NULL/length checks, natural `ORDER BY`,
per-locale `tsvector` columns for FTS, one extra JOIN, and a new locale is pure data.

## Decision
**D for structured/queryable fields, C (JSONB) for content-block payloads.**

```sql
lessons(id, module_id, position, session_type, ...)          -- locale-free structure
lesson_translations(lesson_id, locale, title, summary,
                    search_vector tsvector GENERATED ...)     -- one row per locale
content_blocks(id, lesson_id, position, block_type, settings jsonb)
content_block_translations(block_id, locale, payload jsonb, plain_text, search_vector)
```

Why the split: a lesson *title* is short, sorted, searched and constrained ⇒ a column. A
content block *payload* is polymorphic by `block_type` (a `CODE_EXAMPLE` has
`language`+`code`+`caption`; a `CALLOUT` has `variant`+`body`) ⇒ forcing that into columns
would produce a sparse mess or a table per block type. JSONB + per-type validation
(`ContentBlockPayloadValidator`, one validator per `block_type`) is the honest fit.

`plain_text` is a denormalized, sanitized flattening of `payload` used for full-text search
(ADR-010) and never returned to clients.

**Locale resolution** (`LocaleResolver`): explicit `?locale=` → `Accept-Language` → user's
`preferred_locale` → course `instruction_language` → `uz`. The response always echoes
`resolvedLocale` and `availableLocales`, and sets `isFallback: true` when it fell back, so the
frontend can badge untranslated content instead of silently lying to the student.

**Completeness**: `courses.required_locales text[]` (default `{uz}`). `TranslationCompletenessValidator`
blocks publication when a required locale is missing for any required entity. Optional locales
may be absent — that is the normal state today and must not be papered over with
machine-translated filler.

## Consequences
One JOIN per localized read (negligible; indexed, and cached for the catalogue). ~10 extra
tables — consistent and templated, so an AI agent can copy the pattern. Adding a language is
an INSERT plus a `required_locales` change. Per-locale FTS works properly. Cost: a localized
write touches two tables (handled inside the owning application service).

## Revisit when
A 4th+ locale arrives with a different script needing its own collation, or a client needs the
full multi-locale payload in one response (add a `?locales=all` projection — no schema change).
