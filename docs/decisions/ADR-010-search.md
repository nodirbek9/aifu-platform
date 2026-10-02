# ADR-010 — Search: PostgreSQL full-text, no search engine

**Status:** Accepted

## Context
Search over subjects, courses, lessons and materials (brief §24). Corpus size: 15+ subjects ×
~30 topics × 3 locales ≈ a few thousand documents. That is not a search-engine problem.

## Decision
**PostgreSQL FTS.** Generated `tsvector` columns on each `*_translations` table, GIN-indexed,
combined with `pg_trgm` for typo-tolerant title matching.

```sql
ALTER TABLE lesson_translations ADD COLUMN search_vector tsvector
  GENERATED ALWAYS AS (
     setweight(to_tsvector(fts_config(locale), coalesce(title,'')),   'A') ||
     setweight(to_tsvector(fts_config(locale), coalesce(summary,'')), 'B')
  ) STORED;
CREATE INDEX idx_lesson_tr_fts ON lesson_translations USING gin (search_vector);
```

**Uzbek has no PostgreSQL text-search dictionary.** This is a real limitation, not an
oversight: `fts_config(locale)` is an IMMUTABLE function mapping `ru`→`russian`,
`en`→`english`, `uz`→`simple`. So Uzbek gets tokenization and `unaccent`, but **no stemming**
— "dasturlash" will not match "dasturlar". Mitigations: `pg_trgm` similarity as a second
ranking signal, and a curated `search_synonyms` table for high-traffic terms. A proper Uzbek
dictionary (Snowball/Hunspell) can be installed later without a schema change.

Results are **always authorization-filtered after ranking** (a student searches only published
content in courses they are enrolled in, plus the public catalogue). Search must never become
a way to enumerate unpublished drafts.

## Consequences
Zero extra infrastructure, transactionally consistent (no index lag, no reindex job), ranking
and highlighting via `ts_rank_cd` / `ts_headline`. Weaker Uzbek relevance than a tuned
Elasticsearch; no fuzzy multi-field semantic search.

## Revisit when
The corpus passes ~100k documents, Uzbek relevance is measurably hurting students, or
cross-entity faceted search becomes a product requirement.
