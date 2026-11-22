package com.liftcode.liftcode_server.domain.routine;

import com.liftcode.liftcode_server.domain.User;
import com.liftcode.liftcode_server.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "workouts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Workout extends VersionedEntity {

    public Workout(UUID id, @NonNull User user, @NonNull String name) {
        super(id);
        this.user = user;
        this.name = name;
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
