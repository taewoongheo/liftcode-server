package com.liftcode.liftcode_server.domain.setting;

import com.liftcode.liftcode_server.domain.User;
import com.liftcode.liftcode_server.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "rest_timer_exercise_overrides", uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "exercise_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestTimerExerciseOverride extends VersionedEntity {

    @Id
    private UUID id;

    public RestTimerExerciseOverride(
            @NonNull UUID id,
            @NonNull User user,
            @NonNull String exerciseId,
            Integer duration) {
        this.id = id;
        this.user = user;
        this.exerciseId = exerciseId;
        this.duration = duration;
    }

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "exercise_id", nullable = false,
            check = @CheckConstraint(constraint = "length(trim(exercise_id)) > 0"))
    private String exerciseId;

    @Column(check = @CheckConstraint(constraint = "duration >= 0"))
    private Integer duration;
}
