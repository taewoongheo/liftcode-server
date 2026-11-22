package com.liftcode.liftcode_server.domain.routine;

import com.liftcode.liftcode_server.domain.User;
import com.liftcode.liftcode_server.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "workouts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Workout extends VersionedEntity {

    public Workout(UUID id, User user, String name) {
        super(id);
        this.user = Objects.requireNonNull(user, "user");
        this.name = Objects.requireNonNull(name, "name");
    }

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "workout", cascade = ALL)
    @OrderBy("sortKey ASC")
    private List<WorkoutExercise> workoutExercises;

    @Column(nullable = false)
    private String name;
}
