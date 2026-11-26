package com.liftcode.liftcode_server.workout.domain;

import com.liftcode.liftcode_server.user.domain.User;
import com.liftcode.liftcode_server.common.domain.VersionedEntity;
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
@Table(name = "workouts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Workout extends VersionedEntity {

    @Id
    private UUID id;

    public Workout(@NonNull UUID id, @NonNull User user, @NonNull String name) {
        this.id = id;
        this.user = user;
        this.name = name;
    }

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "workout", cascade = ALL)
    @OrderBy("sortKey ASC")
    private List<WorkoutExercise> workoutExercises = new ArrayList<>();

    @Column(nullable = false, check = @CheckConstraint(constraint = "length(trim(name)) between 1 and 40"))
    private String name;
}
