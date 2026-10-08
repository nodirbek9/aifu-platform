-- ============================================================================
-- V031 — Phase 8.1: Uzbek apostrophes in stored uz text.
--
-- Rewrites the Uzbek orthography of the uz rows of the translation tables, with the same two
-- rules as tools/content/fix-apostrophes.mjs (which does the content files on disk):
--   1. [OoGg] followed by '  `  ‘  ’   ->  that letter + ʻ  (U+02BB)   "Bir o'lchovli" -> "Bir oʻlchovli"
--   2. letter + ' + letter             ->  letter + ʼ  (U+02BC)        "ma'no"        -> "maʼno"
-- Rule 1 runs first, so o/g take the turned comma and every other pair takes the apostrophe.
-- Both are applied until the text stops changing (one pass cannot see `a'b'c`), which makes the
-- rewrite idempotent: the produced letters match neither rule, so a second run is a no-op.
--
-- Tables and columns (all verified against V001-V027; these are the only tables in the schema
-- with a locale column and plain text):
--   subject_translations            title, short_title, summary, description, aim, objectives
--   learning_outcome_translations   statement
--   course_translations             title, description
--   course_module_translations      title, description
--   lesson_translations             title, summary
--   content_block_translations      plain_text                      (see the jsonb note below)
--   activity_translations           title, instructions
--   quiz_translations               title, description, instructions
--   question_translations           prompt, explanation, hint
--   answer_option_translations      text, feedback
--   coding_task_translations        title, description, instructions, constraints_text, explanation
--   coding_task_hint_translations   body
-- The STORED search_vector columns of subject/course/module/lesson/block translations are
-- generated from these columns and recalculate themselves.
--
-- SKIPPED ON PURPOSE — content_block_translations.payload (jsonb, ADR-005). The lesson body text
-- of every block type lives in that polymorphic jsonb document (html / text / caption / code /
-- language), and a blind regexp over jsonb would also rewrite the code samples stored in it. So
-- only the flattened plain_text (FTS input, never serialized to a client) is rewritten here; the
-- bodies are corrected at the source by tools/content/fix-apostrophes.mjs and reach the database
-- again through a content re-import (cd tools/content && node import.mjs ../../content --all).
--
-- Only rows with locale = 'uz' are touched, and only those whose text actually changes
-- (IS DISTINCT FROM), so ru/en rows and already-correct rows are left alone.
--
-- Note on ordering: the dev/demo seeds under db/seed are versions V9xx and therefore run AFTER
-- this migration on a fresh database. Their uz literals (e.g. V901 "Bir o''lchovli massivlar.")
-- are applied migrations and must not be edited, so on a rebuilt database the content re-import
-- above is what brings the text into the corrected orthography.
-- ============================================================================

DROP FUNCTION IF EXISTS public.uz_apostrophes_fix(text);

CREATE FUNCTION public.uz_apostrophes_fix(p_text text)
RETURNS text
LANGUAGE plpgsql
IMMUTABLE
AS $fn$
DECLARE
    v_current text := p_text;
    v_next    text;
BEGIN
    IF v_current IS NULL THEN
        RETURN NULL;
    END IF;
    LOOP
        v_next := regexp_replace(
                      regexp_replace(v_current, $re1$([OoGg])['`‘’]$re1$, '\1ʻ', 'g'),
                      $re2$([[:alpha:]])'([[:alpha:]])$re2$, '\1ʼ\2', 'g');
        EXIT WHEN v_next = v_current;
        v_current := v_next;
    END LOOP;
    RETURN v_current;
END
$fn$;

UPDATE subject_translations
   SET title       = public.uz_apostrophes_fix(title),
       short_title = public.uz_apostrophes_fix(short_title),
       summary     = public.uz_apostrophes_fix(summary),
       description = public.uz_apostrophes_fix(description),
       aim         = public.uz_apostrophes_fix(aim),
       objectives  = public.uz_apostrophes_fix(objectives)
 WHERE locale = 'uz'
   AND (   title       IS DISTINCT FROM public.uz_apostrophes_fix(title)
        OR short_title IS DISTINCT FROM public.uz_apostrophes_fix(short_title)
        OR summary     IS DISTINCT FROM public.uz_apostrophes_fix(summary)
        OR description IS DISTINCT FROM public.uz_apostrophes_fix(description)
        OR aim         IS DISTINCT FROM public.uz_apostrophes_fix(aim)
        OR objectives  IS DISTINCT FROM public.uz_apostrophes_fix(objectives));

UPDATE learning_outcome_translations
   SET statement = public.uz_apostrophes_fix(statement)
 WHERE locale = 'uz'
   AND statement IS DISTINCT FROM public.uz_apostrophes_fix(statement);

UPDATE course_translations
   SET title       = public.uz_apostrophes_fix(title),
       description = public.uz_apostrophes_fix(description)
 WHERE locale = 'uz'
   AND (   title       IS DISTINCT FROM public.uz_apostrophes_fix(title)
        OR description IS DISTINCT FROM public.uz_apostrophes_fix(description));

UPDATE course_module_translations
   SET title       = public.uz_apostrophes_fix(title),
       description = public.uz_apostrophes_fix(description)
 WHERE locale = 'uz'
   AND (   title       IS DISTINCT FROM public.uz_apostrophes_fix(title)
        OR description IS DISTINCT FROM public.uz_apostrophes_fix(description));

UPDATE lesson_translations
   SET title   = public.uz_apostrophes_fix(title),
       summary = public.uz_apostrophes_fix(summary)
 WHERE locale = 'uz'
   AND (   title   IS DISTINCT FROM public.uz_apostrophes_fix(title)
        OR summary IS DISTINCT FROM public.uz_apostrophes_fix(summary));

-- plain_text only: payload is jsonb and holds code samples next to the text (see the header).
UPDATE content_block_translations
   SET plain_text = public.uz_apostrophes_fix(plain_text)
 WHERE locale = 'uz'
   AND plain_text IS DISTINCT FROM public.uz_apostrophes_fix(plain_text);

UPDATE activity_translations
   SET title        = public.uz_apostrophes_fix(title),
       instructions = public.uz_apostrophes_fix(instructions)
 WHERE locale = 'uz'
   AND (   title        IS DISTINCT FROM public.uz_apostrophes_fix(title)
        OR instructions IS DISTINCT FROM public.uz_apostrophes_fix(instructions));

UPDATE quiz_translations
   SET title        = public.uz_apostrophes_fix(title),
       description  = public.uz_apostrophes_fix(description),
       instructions = public.uz_apostrophes_fix(instructions)
 WHERE locale = 'uz'
   AND (   title        IS DISTINCT FROM public.uz_apostrophes_fix(title)
        OR description  IS DISTINCT FROM public.uz_apostrophes_fix(description)
        OR instructions IS DISTINCT FROM public.uz_apostrophes_fix(instructions));

UPDATE question_translations
   SET prompt      = public.uz_apostrophes_fix(prompt),
       explanation = public.uz_apostrophes_fix(explanation),
       hint        = public.uz_apostrophes_fix(hint)
 WHERE locale = 'uz'
   AND (   prompt      IS DISTINCT FROM public.uz_apostrophes_fix(prompt)
        OR explanation IS DISTINCT FROM public.uz_apostrophes_fix(explanation)
        OR hint        IS DISTINCT FROM public.uz_apostrophes_fix(hint));

UPDATE answer_option_translations
   SET text     = public.uz_apostrophes_fix(text),
       feedback = public.uz_apostrophes_fix(feedback)
 WHERE locale = 'uz'
   AND (   text     IS DISTINCT FROM public.uz_apostrophes_fix(text)
        OR feedback IS DISTINCT FROM public.uz_apostrophes_fix(feedback));

UPDATE coding_task_translations
   SET title            = public.uz_apostrophes_fix(title),
       description      = public.uz_apostrophes_fix(description),
       instructions     = public.uz_apostrophes_fix(instructions),
       constraints_text = public.uz_apostrophes_fix(constraints_text),
       explanation      = public.uz_apostrophes_fix(explanation)
 WHERE locale = 'uz'
   AND (   title            IS DISTINCT FROM public.uz_apostrophes_fix(title)
        OR description      IS DISTINCT FROM public.uz_apostrophes_fix(description)
        OR instructions     IS DISTINCT FROM public.uz_apostrophes_fix(instructions)
        OR constraints_text IS DISTINCT FROM public.uz_apostrophes_fix(constraints_text)
        OR explanation      IS DISTINCT FROM public.uz_apostrophes_fix(explanation));

UPDATE coding_task_hint_translations
   SET body = public.uz_apostrophes_fix(body)
 WHERE locale = 'uz'
   AND body IS DISTINCT FROM public.uz_apostrophes_fix(body);

DROP FUNCTION public.uz_apostrophes_fix(text);
