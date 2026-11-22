-- Apply once to an existing database using the original integer positions.
-- Fresh databases created from the entities already have the new columns.
BEGIN;
LOCK TABLE exercise_logs, set_logs IN ACCESS EXCLUSIVE MODE;
ALTER TABLE exercise_logs ADD COLUMN sort_key text COLLATE "C";
ALTER TABLE set_logs ADD COLUMN sort_key text COLLATE "C";

-- Fixed-width digit prefixes preserve numeric order under C collation.
-- The nonzero suffix makes every backfilled key a canonical fraction.
WITH ranked AS (
    SELECT id, row_number() OVER (PARTITION BY workout_log_id ORDER BY position, id) AS ordinal
    FROM exercise_logs
)
UPDATE exercise_logs AS item SET sort_key = lpad(to_hex(ranked.ordinal), 16, '0') || 'V'
FROM ranked WHERE item.id = ranked.id;
WITH ranked AS (
    SELECT id, row_number() OVER (PARTITION BY exercise_log_id ORDER BY position, id) AS ordinal
    FROM set_logs
)
UPDATE set_logs AS item SET sort_key = lpad(to_hex(ranked.ordinal), 16, '0') || 'V'
FROM ranked WHERE item.id = ranked.id;
ALTER TABLE exercise_logs ALTER COLUMN sort_key SET NOT NULL,
    ADD UNIQUE (workout_log_id, sort_key), DROP COLUMN position;
ALTER TABLE set_logs ALTER COLUMN sort_key SET NOT NULL,
    ADD UNIQUE (exercise_log_id, sort_key), DROP COLUMN position;
COMMIT;
