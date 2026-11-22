package com.liftcode.liftcode_server.domain.routine;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "workout_exercises", uniqueConstraints = @UniqueConstraint(columnNames = {"workout_id", "sort_key"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutExercise extends OrderedRoutineItem {

    public WorkoutExercise(
            UUID id,
            @NonNull Workout workout,
            @NonNull String exerciseKey,
            String sortKey,
            boolean supersetWithNext) {
        super(id, sortKey);
        this.workout = workout;
        this.exerciseKey = exerciseKey;
        this.supersetWithNext = supersetWithNext;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    @OneToMany(mappedBy = "workoutExercise", cascade = ALL)
    @OrderBy("sortKey ASC")
    private List<WorkoutSet> workoutSets = new ArrayList<>();

    @Column(nullable = false)
    private String exerciseKey;

    @Column(nullable = false)
    private boolean supersetWithNext;
}
