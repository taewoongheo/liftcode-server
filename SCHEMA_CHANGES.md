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
