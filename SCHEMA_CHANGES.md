# Schema Changes

Server schema changes to account for when updating the LiftCode client.

## Fractional indexing for ordered items

- Server mappings use a string `sort_key` in place of integer `position` for workout exercises, workout sets, exercise logs, and set logs. Keys sort in ascending order and are unique within the parent list. `ProgramItem` also uses the shared `OrderedItem` mapping.
- When migrating existing rows, assign keys in the original `position` order within each parent list. When updating the client, preserve list order across sync and use fractional keys wherever the client needs to generate or compare server sort keys.

## Standalone workouts

Store a standalone workout directly in `workouts`. When migrating client programs with `training_mode = 'workout'`, keep the workout ID and update references to the wrapper program. Create and sync standalone workouts without a program row or a `training_mode` field.

## Program progress and completions (planned)

- Keep program-level start and restart timestamps on `programs`. Omit `program_runs` from the new server schema and derive the next item from the latest completion since the last restart and the current program order. Start at the first item when there is no completion and wrap after the last item.
- Migrate workout completion details and rest completions into `training_logs` with type `WORKOUT` or `REST`. Repoint exercise logs to `training_log_id`. Preserve the program ID, item ID, program name, and item position at completion. Standalone workouts have no program context.
- Capture and sync both `started_at` and `completed_at` for rest entries. Existing client rest records contain only `completed_at`; handling missing historical start times remains a migration decision.
- Update scheduling and sync to read completion history from the logs.

## Default gym

The server stores the default gym in `users.default_gym_id` and has removed `gym_settings`. Migrate each user's existing selection and sync future selection changes with the user version.

- Deleting the selected gym clears `users.default_gym_id` (`ON DELETE SET NULL`).
- Deleting a user deletes all gyms they own through `gyms.user_id` (`ON DELETE CASCADE`).

## Enum values

Server enum columns store the uppercase Java constant name. Update client storage and sync payloads to use these values.

| Field | Server values |
| --- | --- |
| Body profile sex | `FEMALE`, `MALE` |
| Distance unit | `KM`, `MI` |
| Equipment and set log weight units | `KG`, `LB` |
| Equipment weight method | `RANGE`, `BAR_AND_PLATES` |
| Workout and logged set type | `WORKING`, `WARMUP`, `DROP`, `MYO`, `FAILURE` |
| Exercise log tracking type | `REPS`, `WEIGHT_REPS`, `TIME`, `TIME_DISTANCE` |

## Equipment weight range positions

Keep each range's stored integer `position` when loading, editing, and syncing it. Display ranges in ascending position order. The client currently rewrites the full list using array indices. Update it to insert, edit, or delete only the affected rows. Preserve gaps after deletion and append new ranges at `max(position) + 1` for the same gym and equipment, starting at 0 for an empty list.

## Equipment bar weight

Each gym and equipment pair has at most one bar weight. Store it in the nullable `gym_equipment_weight_settings.bar_weight` column alongside `bar_unit`. When migrating, copy the existing value by `(gym_id, equipment_id)` before dropping `gym_equipment_bar_settings`. A missing row maps to null. Switching weight methods preserves the saved bar weight.

## Warmup schemes

- Merge `warmup_default_settings` and `exercise_warmup_settings` into `warmup_schemes`. A user default uses the user's ID as its primary key and has a null `exercise_key`. Exercise overrides are unique per user and exercise.
- Merge `warmup_default_steps` and `exercise_warmup_steps` into `warmup_steps`. Preserve each step's position, weight percentage, and optional rep count under its `scheme_id`. Saving edits replaces the scheme's ordered list.

Reset a customization by deleting its scheme. Resolve an exercise override first, then the user's default, then the built-in 40% × 8, 60% × 5, and 80% × 3.

## Goals, body measurements and exercise notes

- Use the existing deterministic exercise-note sync UUID as the primary key of `exercise_notes`. The pair `(user_id, exercise_key)` stays unique. Clearing a note keeps its ID and stores null.
- Keep the user's local `measured_on` date when syncing a body measurement. The server manages its audit timestamps separately from the time of measurement.
- Send the user's selected distance unit explicitly. The server defaults to `KM` and a fresh client database defaults to `mi`.
