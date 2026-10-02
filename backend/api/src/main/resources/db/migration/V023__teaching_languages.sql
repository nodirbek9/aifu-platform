-- ============================================================================
-- V023 — The teaching languages are Python, Java and C++ (product spec S-05.2).
--
-- V010 enabled Go because the first brief named "Python/Go". The owner has since settled on
-- Python, Java and C++ (V018, V020), no task carries a Go solution, and an enabled Go would
-- appear in the language list of the editor. The row stays (old submissions may reference it)
-- but is no longer offered.
-- ============================================================================

UPDATE programming_languages
SET is_enabled = false,
    notes = 'Not a teaching language (product decision 2026-10-02: Python, Java, C++). Kept for history.'
WHERE code = 'go';
