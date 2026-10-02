-- ============================================================================
-- V020 — C++ as the third teaching language.
--
-- Product decision (2026-10-02, docs/01-PRODUCT-SPEC.md S-05): students solve coding tasks in
-- Python, Java or C++. V010 seeded the cpp row DISABLED because the DAS1110 syllabus is a C++
-- course while the first brief named Python/Go; the owner has now settled it. Go stays enabled as
-- a row but is not offered by the product (no task carries a Go solution).
--
-- executor_key keeps V010's value (Judge0 language id 54, GCC C++17). The docker-run engine maps
-- the code to an image in DockerLanguageSpec (gcc:13, -std=c++17).
-- ============================================================================

UPDATE programming_languages
SET is_enabled = true,
    notes = 'Teaching language alongside Python and Java (product decision 2026-10-02); the DAS1110 syllabus is a C++ course.'
WHERE code = 'cpp';
