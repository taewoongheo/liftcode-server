package com.liftcode.liftcode_server.domain.log;

import com.liftcode.liftcode_server.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@DiscriminatorValue("WORKOUT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutLog extends TrainingLog {

    public WorkoutLog(UUID id, User user, Instant startedAt, Instant completedAt, @NonNull UUID sourceWorkoutId,
                      @NonNull String workoutNameSnapshot, int durationSeconds, @NonNull String notes) {
        super(id, user, startedAt, completedAt);
        this.sourceWorkoutId = sourceWorkoutId;
        this.workoutNameSnapshot = workoutNameSnapshot;
        this.durationSeconds = durationSeconds;
        this.notes = notes;
    }

    public WorkoutLog(UUID id, User user, Instant startedAt, Instant completedAt,
                      UUID sourceWorkoutId, String workoutNameSnapshot, int durationSeconds, String notes,
                      UUID programId, UUID programItemId, String programNameSnapshot, int itemPositionSnapshot) {
        this(id, user, startedAt, completedAt, sourceWorkoutId, workoutNameSnapshot, durationSeconds, notes);
        setProgramSnapshot(programId, programItemId, programNameSnapshot, itemPositionSnapshot);
    }

    @OneToMany(mappedBy = "workoutLog", cascade = ALL)
    @OrderBy("sortKey ASC")
    private List<ExerciseLog> exerciseLogs = new ArrayList<>();

    private UUID sourceWorkoutId;
    private String workoutNameSnapshot;
    private Integer durationSeconds;
    private String notes;
}
