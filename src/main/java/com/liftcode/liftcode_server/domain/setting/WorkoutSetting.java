package com.liftcode.liftcode_server.domain.setting;

import com.liftcode.liftcode_server.domain.User;
import com.liftcode.liftcode_server.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "workout_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutSetting extends VersionedEntity {

    @Id
    private UUID id;

    @MapsId
    @OneToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean rirTrackingEnabled;

    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean restTimerEnabled;

    public WorkoutSetting(
            @NonNull User user,
            boolean rirTrackingEnabled,
            boolean restTimerEnabled) {
        this.id = user.getId();
        this.user = user;
        this.rirTrackingEnabled = rirTrackingEnabled;
        this.restTimerEnabled = restTimerEnabled;
    }
}
