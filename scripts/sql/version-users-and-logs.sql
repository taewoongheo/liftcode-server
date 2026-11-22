BEGIN;
-- Existing records have no audit timestamps. Both start at the migration time.
ALTER TABLE users
    ADD COLUMN version integer NOT NULL DEFAULT 0,
    ADD COLUMN created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP;
-- Workout logs already have a version. Keep existing values.
ALTER TABLE workout_logs
    ALTER COLUMN version SET DEFAULT 0,
    ADD COLUMN created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE exercise_logs
    ADD COLUMN version integer NOT NULL DEFAULT 0,
    ADD COLUMN created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE set_logs
    ADD COLUMN version integer NOT NULL DEFAULT 0,
    ADD COLUMN created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP;
COMMIT;
