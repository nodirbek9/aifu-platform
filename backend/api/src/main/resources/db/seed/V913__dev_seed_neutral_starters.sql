-- ============================================================================
-- V913 (DEV ONLY) — Starter code of coding tasks carries no natural-language comments.
--
-- The starter of a coding task is shown as-is in every interface language, but its comments
-- were written in Uzbek ("// TODO: natijani chiqaring"), so a student working in Russian or
-- English read Uzbek inside the code editor. Starter code is not localized and adding a
-- per-locale starter is not worth a schema change, so the comments become language-neutral
-- marker lines: "# ..." (Python) and "// ..." (Java, C++, Go). Signatures, includes and the
-- skeleton stay exactly as they were, so every starter still compiles and still fails the tests.
--
-- The same comments are removed from the sources in tools/content-seed/content/_cpp.mjs, so a
-- fresh database built from V902/V906/V907 and then V913 ends up identical to the sources.
-- Applied migrations are never edited here. The statement is idempotent: a row whose starter is
-- already neutral is not touched. solution_code (teacher-only, never sent to a student) is not.
--
-- Only comments that start a line are rewritten: "#" is a comment in Python and a preprocessor
-- directive in C++ ("#include"), "//" is a comment in Java/C++/Go and floor division in Python.
--
-- Check afterwards (must return 0):
--   SELECT count(*) FROM coding_task_languages
--   WHERE starter_code ~ E'(^|\n)[ \t]*(#[ \t]?[^. \t]|//[ \t]?[^. \t])';
-- ============================================================================

-- Python: every line-start comment becomes "# ..."
UPDATE coding_task_languages
SET starter_code = regexp_replace(starter_code, E'(^|\n)([ \t]*)#[^\n]*', E'\\1\\2# ...', 'g')
WHERE language_code = 'python'
  AND starter_code IS NOT NULL
  AND starter_code ~ E'(^|\n)[ \t]*#[ \t]?[^. \t]';

-- Java, C++, Go: every line-start comment becomes "// ..."
UPDATE coding_task_languages
SET starter_code = regexp_replace(starter_code, E'(^|\n)([ \t]*)//[^\n]*', E'\\1\\2// ...', 'g')
WHERE language_code <> 'python'
  AND starter_code IS NOT NULL
  AND starter_code ~ E'(^|\n)[ \t]*//[ \t]?[^. \t]';
