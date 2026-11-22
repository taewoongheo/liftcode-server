package com.liftcode.liftcode_server.domain.routine;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "workout_sets")
public class WorkoutSet {

    @Id
    private UUID id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "workout_exercise_id")
    private WorkoutExercise workoutExercise;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_workout_set_id")
    private WorkoutSet parentWorkoutSet;

    @OneToMany(mappedBy = "parentWorkoutSet", cascade = ALL)
    private List<WorkoutSet> workoutSets;

    @Column(nullable = false)
    private int position;

    // setType 은 enum 으로 만들어야된다고 생각
    // enum 타입에 맞게 입력받는 target 값을 나누어야 된다고 생각
    @Column(nullable = false)
    private String setType;

    private Integer targetRepsMin;

    private Integer targetRepsMax;

    private Integer targetDurationSeconds;

    private Double targetDistance;
}
