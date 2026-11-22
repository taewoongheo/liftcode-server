package com.liftcode.liftcode_server.domain.routine;

import com.liftcode.liftcode_server.domain.User;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "workouts")
public class Workout {

    @Id
    private UUID id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "workout", cascade = ALL)
    private List<WorkoutExercise> workoutExercises;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int version;
}
