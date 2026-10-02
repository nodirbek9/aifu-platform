-- ============================================================================
-- V018 — Java as a second teaching language next to Python.
--
-- Product decision (2026-09-28): students solve coding tasks in Python or Java. The row is
-- reference data like the V010 languages; executor_key follows V010's convention and names
-- the Judge0 language id for Java (62). The docker-run engine does not read executor_key —
-- it maps the language code to an image in DockerLanguageSpec (eclipse-temurin:21-jdk).
-- Submissions must declare `public class Main` because source_filename is Main.java.
-- ============================================================================

INSERT INTO programming_languages
 (code, display_name, version_label, executor_key, compile_required, source_filename, is_enabled, notes)
VALUES
 ('java', 'Java', '21', 'judge0:62', true, 'Main.java', true,
  'Teaching language alongside Python (product decision 2026-09-28). The class must be public class Main.')
ON CONFLICT (code) DO NOTHING;
