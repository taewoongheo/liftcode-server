package com.liftcode.liftcode_server.domain.routine;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "workout_exercises")
public class WorkoutExercise {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    @OneToMany(mappedBy = "workoutExercise", cascade = ALL)
    private List<WorkoutSet> workoutSets;

    @Column(nullable = false)
    private String exerciseKey;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private boolean supersetWithNext;
}
