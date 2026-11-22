package com.liftcode.liftcode_server.domain.routine;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "workout_sets", uniqueConstraints = @UniqueConstraint(columnNames = {"workout_exercise_id", "sort_key"}))
@Getter
public class WorkoutSet extends OrderedRoutineItem {

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "workout_exercise_id")
    private WorkoutExercise workoutExercise;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_workout_set_id")
    private WorkoutSet parentWorkoutSet;

    @OneToMany(mappedBy = "parentWorkoutSet", cascade = ALL)
    @OrderBy("sortKey ASC")
    private List<WorkoutSet> workoutSets;

    // TODO: setType 은 enum 으로 변경하기
    @Column(nullable = false)
    private String setType;

    private Integer targetRepsMin;

    private Integer targetRepsMax;

    private Integer targetDurationSeconds;

    private Double targetDistance;
}
