package com.liftcode.liftcode_server.domain.routine;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "workout_exercises", uniqueConstraints = @UniqueConstraint(columnNames = {"workout_id", "sort_key"}))
@Getter
public class WorkoutExercise extends OrderedRoutineItem {

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
