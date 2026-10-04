-- ============================================================================
-- V025 — A deleted module, lesson or block no longer holds its position.
-- ============================================================================
-- Modules, lessons and content blocks are soft-deleted: the row stays with deleted_at set. The
-- unique (parent, position) constraints still counted those rows, so after a deletion a new item
-- or a reorder could collide with the position of a deleted one (409 on "add a block"). The
-- uniqueness now holds among live rows only. An exclusion constraint, unlike a partial unique
-- index, can stay DEFERRABLE INITIALLY DEFERRED, which the reorder (positions swapped inside one
-- transaction) relies on.

ALTER TABLE content_blocks DROP CONSTRAINT uq_content_block_position;
ALTER TABLE content_blocks ADD CONSTRAINT uq_content_block_position
    EXCLUDE USING btree (lesson_id WITH =, position WITH =) WHERE (deleted_at IS NULL)
    DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE lessons DROP CONSTRAINT uq_lesson_position;
ALTER TABLE lessons ADD CONSTRAINT uq_lesson_position
    EXCLUDE USING btree (module_id WITH =, position WITH =) WHERE (deleted_at IS NULL)
    DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE course_modules DROP CONSTRAINT uq_module_position;
ALTER TABLE course_modules ADD CONSTRAINT uq_module_position
    EXCLUDE USING btree (course_id WITH =, position WITH =) WHERE (deleted_at IS NULL)
    DEFERRABLE INITIALLY DEFERRED;
