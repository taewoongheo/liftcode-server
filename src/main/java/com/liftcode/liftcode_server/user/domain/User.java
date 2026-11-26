package com.liftcode.liftcode_server.user.domain;

import com.liftcode.liftcode_server.common.domain.VersionedEntity;
import com.liftcode.liftcode_server.gym.domain.Gym;
import com.liftcode.liftcode_server.program.domain.Program;
import com.liftcode.liftcode_server.workout.domain.Workout;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "users", check = @CheckConstraint(
        name = "users_at_most_one_training_selection",
        constraint = "workout_id IS NULL OR program_id IS NULL"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends VersionedEntity {

    @Id
    private UUID id;

    public User(@NonNull UUID id) {
        this.id = id;
    }

    /**
     * TODO: Clear and flush the selected gym in the deletion service to advance the user version.
     */
    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "default_gym_id", unique = true)
    private Gym defaultGym;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "workoutId")
    private Workout workout;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "programId")
    private Program program;

    public void setDefaultGym(Gym gym) {
        if (gym != null && !getId().equals(gym.getUser().getId())) {
            throw new IllegalArgumentException("Default gym must belong to the user");
        }
        defaultGym = gym;
    }

}
