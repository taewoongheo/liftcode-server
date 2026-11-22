package com.liftcode.liftcode_server.domain.routine;

import com.liftcode.liftcode_server.domain.User;
import com.liftcode.liftcode_server.domain.VersionedEntity;
import jakarta.persistence.*;

import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "workouts")
public class Workout extends VersionedEntity {

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "workout", cascade = ALL)
    @OrderBy("sortKey ASC")
    private List<WorkoutExercise> workoutExercises;

    @Column(nullable = false)
    private String name;
}
