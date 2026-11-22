-- Apply once to an existing database using the original integer positions.
-- Fresh databases created from the entities already have the new columns.
BEGIN;
LOCK TABLE workouts, workout_exercises, workout_sets IN ACCESS EXCLUSIVE MODE;
ALTER TABLE workout_exercises ADD COLUMN sort_key text COLLATE "C", ADD COLUMN version integer NOT NULL DEFAULT 0;
ALTER TABLE workout_sets ADD COLUMN sort_key text COLLATE "C", ADD COLUMN version integer NOT NULL DEFAULT 0;

-- Fixed-width digit prefixes preserve numeric order under C collation.
-- The nonzero suffix makes every backfilled key a canonical fraction.
WITH ranked AS (
    SELECT id, row_number() OVER (PARTITION BY workout_id ORDER BY position, id) AS ordinal
    FROM workout_exercises
)
UPDATE workout_exercises AS item SET sort_key = lpad(to_hex(ranked.ordinal), 16, '0') || 'V'
FROM ranked WHERE item.id = ranked.id;
WITH ranked AS (
    SELECT id, row_number() OVER (PARTITION BY workout_exercise_id ORDER BY position, id) AS ordinal
    FROM workout_sets
)
UPDATE workout_sets AS item SET sort_key = lpad(to_hex(ranked.ordinal), 16, '0') || 'V'
FROM ranked WHERE item.id = ranked.id;
ALTER TABLE workout_exercises ALTER COLUMN sort_key SET NOT NULL,
    ADD UNIQUE (workout_id, sort_key), DROP COLUMN position;
ALTER TABLE workout_sets ALTER COLUMN sort_key SET NOT NULL,
    ADD UNIQUE (workout_exercise_id, sort_key), DROP COLUMN position;
COMMIT;
