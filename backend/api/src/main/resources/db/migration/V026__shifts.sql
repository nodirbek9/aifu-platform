-- ============================================================================
-- V026 — Shifts ("smena") in the bell schedule.
--
-- AIFU teaches in three shifts, each with its own pairs of 80 minutes:
--   morning    09:00–10:20, 10:35–11:55, 12:10–13:30   (15-minute breaks)
--   afternoon  13:30–14:50, 15:00–16:20, 16:30–17:50   (10-minute breaks)
--   evening    18:00–19:20, 19:30–20:50                (10-minute breaks, two pairs only)
--
-- pair_no stays the pair's place in the whole day (1…8): timetable entries, class sessions and
-- notifications keep pointing at the same rows. shift_pair_no is what people call the pair
-- ("1-para" of the afternoon shift). A group studies in one shift.
-- ============================================================================

ALTER TABLE pair_slots
    ADD COLUMN shift text NOT NULL DEFAULT 'MORNING'
        CHECK (shift IN ('MORNING', 'AFTERNOON', 'EVENING')),
    ADD COLUMN shift_pair_no int;

-- the six default rows of V019: 1–3 become the morning shift at its real times, 4–6 the
-- afternoon shift (their times were already right)
UPDATE pair_slots SET shift = 'MORNING', shift_pair_no = 1, starts_at = '09:00', ends_at = '10:20' WHERE pair_no = 1;
UPDATE pair_slots SET shift = 'MORNING', shift_pair_no = 2, starts_at = '10:35', ends_at = '11:55' WHERE pair_no = 2;
UPDATE pair_slots SET shift = 'MORNING', shift_pair_no = 3, starts_at = '12:10', ends_at = '13:30' WHERE pair_no = 3;
UPDATE pair_slots SET shift = 'AFTERNOON', shift_pair_no = 1, starts_at = '13:30', ends_at = '14:50' WHERE pair_no = 4;
UPDATE pair_slots SET shift = 'AFTERNOON', shift_pair_no = 2, starts_at = '15:00', ends_at = '16:20' WHERE pair_no = 5;
UPDATE pair_slots SET shift = 'AFTERNOON', shift_pair_no = 3, starts_at = '16:30', ends_at = '17:50' WHERE pair_no = 6;
-- any other row an administrator added keeps its time and joins the shift it starts in
UPDATE pair_slots
SET shift = CASE WHEN starts_at < '13:30' THEN 'MORNING'
                 WHEN starts_at < '18:00' THEN 'AFTERNOON'
                 ELSE 'EVENING' END
WHERE pair_no > 6;
INSERT INTO pair_slots (pair_no, shift, starts_at, ends_at)
SELECT v.pair_no, 'EVENING', v.starts_at, v.ends_at
FROM (VALUES (7, time '18:00', time '19:20'), (8, time '19:30', time '20:50')) AS v(pair_no, starts_at, ends_at)
WHERE NOT EXISTS (SELECT 1 FROM pair_slots p WHERE p.pair_no = v.pair_no);
UPDATE pair_slots p
SET shift_pair_no = n.rn
FROM (SELECT id, row_number() OVER (PARTITION BY shift ORDER BY starts_at) AS rn
      FROM pair_slots) n
WHERE n.id = p.id AND p.shift_pair_no IS NULL;
-- a row that came in after the six defaults may now share a number within its shift: renumber
-- every shift by start time so that (shift, shift_pair_no) is unique
UPDATE pair_slots p
SET shift_pair_no = n.rn
FROM (SELECT id, row_number() OVER (PARTITION BY shift ORDER BY starts_at, pair_no) AS rn
      FROM pair_slots) n
WHERE n.id = p.id;

ALTER TABLE pair_slots
    ALTER COLUMN shift_pair_no SET NOT NULL,
    ADD CONSTRAINT ck_pair_slot_shift_no CHECK (shift_pair_no BETWEEN 1 AND 6),
    ADD CONSTRAINT uq_pair_slot_shift_no UNIQUE (shift, shift_pair_no);

COMMENT ON COLUMN pair_slots.pair_no IS 'The pair''s place in the whole day (1…12); stable reference.';
COMMENT ON COLUMN pair_slots.shift IS 'MORNING, AFTERNOON or EVENING.';
COMMENT ON COLUMN pair_slots.shift_pair_no IS 'The number people use: the pair''s place within its shift.';

ALTER TABLE student_groups
    ADD COLUMN shift text NOT NULL DEFAULT 'MORNING'
        CHECK (shift IN ('MORNING', 'AFTERNOON', 'EVENING'));
COMMENT ON COLUMN student_groups.shift IS 'The shift the group studies in.';

-- a group whose classes already sit in one other shift belongs to that shift
UPDATE student_groups g
SET shift = s.shift
FROM (SELECT teg.group_id, min(p.shift) AS shift
      FROM timetable_entry_groups teg
      JOIN timetable_entries e ON e.id = teg.entry_id
      JOIN pair_slots p ON p.id = e.pair_slot_id
      GROUP BY teg.group_id
      HAVING count(DISTINCT p.shift) = 1) s
WHERE s.group_id = g.id;
