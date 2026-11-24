package com.liftcode.liftcode_server.domain.workout;

import com.liftcode.liftcode_server.domain.OrderedItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "workout_sets",
        uniqueConstraints = @UniqueConstraint(columnNames = {"workout_exercise_id", "sort_key"}),
        check = {
                @CheckConstraint(name = "workout_sets_reps_order", constraint = "target_reps_min <= target_reps_max"),
                @CheckConstraint(name = "workout_sets_not_own_parent", constraint = "parent_workout_set_id <> id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutSet extends OrderedItem {

    public WorkoutSet(UUID id, @NonNull WorkoutExercise workoutExercise, @NonNull SetType setType, String sortKey) {
        super(id, sortKey);
        this.workoutExercise = workoutExercise;
        this.setType = setType;
    }

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "workout_exercise_id")
    private WorkoutExercise workoutExercise;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_workout_set_id")
    private WorkoutSet parentWorkoutSet;

    @OneToMany(mappedBy = "parentWorkoutSet", cascade = ALL)
    @OrderBy("sortKey ASC")
    private List<WorkoutSet> workoutSets = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SetType setType;

    @Column(check = @CheckConstraint(constraint = "target_reps_min between 0 and 9999"))
    private Integer targetRepsMin;

    @Column(check = @CheckConstraint(constraint = "target_reps_max between 0 and 9999"))
    private Integer targetRepsMax;

    private Integer targetDurationSeconds;

    private Double targetDistance;
}
