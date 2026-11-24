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

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "rest_timer_exercise_overrides")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestTimerExerciseOverride extends VersionedEntity {

    @EmbeddedId
    private RestTimerExerciseOverrideId id;

    @MapsId("userId")
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(check = @CheckConstraint(constraint = "duration >= 0"))
    private Integer duration;

    public RestTimerExerciseOverride(
            @NonNull User user,
            @NonNull String exerciseId,
            Integer duration) {
        this.id = new RestTimerExerciseOverrideId(user.getId(), exerciseId);
        this.user = user;
        this.duration = duration;
    }
}
