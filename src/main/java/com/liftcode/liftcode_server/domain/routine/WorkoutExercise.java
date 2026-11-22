package com.liftcode.liftcode_server.domain.routine;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "workout_exercises", uniqueConstraints = @UniqueConstraint(columnNames = {"workout_id", "sort_key"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutExercise extends OrderedRoutineItem {

    public WorkoutExercise(UUID id, Workout workout, String exerciseKey, String sortKey, boolean supersetWithNext) {
        super(id, sortKey);
        this.workout = Objects.requireNonNull(workout, "workout");
        this.exerciseKey = Objects.requireNonNull(exerciseKey, "exerciseKey");
        this.supersetWithNext = supersetWithNext;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    @OneToMany(mappedBy = "workoutExercise", cascade = ALL)
    @OrderBy("sortKey ASC")
    private List<WorkoutSet> workoutSets;

    @Column(nullable = false)
    private String exerciseKey;

    @Column(nullable = false)
    private boolean supersetWithNext;
}
