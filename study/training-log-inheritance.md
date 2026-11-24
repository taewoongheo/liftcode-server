# One table for workout and rest history

**Before.** Separate tables repeated the user and timestamps. Reading one user's history in completion order meant combining both tables.

```sql
-- History query with the two-table design
SELECT completed_at FROM workout_logs WHERE user_id = :user_id
UNION ALL
SELECT completed_at FROM rest_logs WHERE user_id = :user_id
ORDER BY completed_at;
```

**Decision.** One row in `training_logs` represents one completed activity. Both kinds share the user, timestamps, and optional program context. The `type` column distinguishes `WORKOUT` from `REST`, so one query returns the whole timeline.

**Trade-off.** Workout-only columns such as `source_workout_id` must allow null for rest rows. JPA single-table inheritance keeps those fields and the exercise collection on `WorkoutLog`, while `RestLog` uses only the shared fields.
