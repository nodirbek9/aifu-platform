-- ============================================================================
-- V036 — Timetable ADD exception (phase 7, chunk 7.5-B1).
--
-- Allows one-off additional class occurrences (ExceptionKind.ADD) on a specific date.
-- For ADD exceptions:
--   - new_pair_slot_id is required
--   - new_date is absent (NULL)
--   - reason is required (1-300 characters after trimming)
--   - new_room_id and new_teacher_user_id are optional overrides
--   - (entry_id, on_date) uniqueness is preserved from V019
-- ============================================================================

ALTER TABLE timetable_exceptions DROP CONSTRAINT timetable_exceptions_kind_check;
ALTER TABLE timetable_exceptions ADD CONSTRAINT timetable_exceptions_kind_check
    CHECK (kind IN ('CANCELLED', 'MOVED', 'REPLACED', 'ADD'));

ALTER TABLE timetable_exceptions ADD CONSTRAINT ck_tt_exception_add
    CHECK (kind <> 'ADD' OR (new_pair_slot_id IS NOT NULL AND new_date IS NULL AND reason IS NOT NULL AND length(btrim(reason)) BETWEEN 1 AND 300));
