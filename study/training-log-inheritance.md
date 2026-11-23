# JPA single-table inheritance for training logs

- **Problem.** One concrete `TrainingLog` mixed workout and rest constructors and allowed exercise entries on rest logs.
- **Decision.** `WorkoutLog` and `RestLog` extend `TrainingLog`. JPA uses the `type` column in `training_logs` to load the right class. `ExerciseLog` accepts only a `WorkoutLog`.
