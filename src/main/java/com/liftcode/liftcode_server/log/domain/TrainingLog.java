package com.liftcode.liftcode_server.log.domain;

import com.liftcode.liftcode_server.user.domain.User;
import com.liftcode.liftcode_server.common.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Instant;
import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@Table(name = "training_logs", check = {
        @CheckConstraint(name = "training_logs_time_order", constraint = "completed_at >= started_at"),
        @CheckConstraint(name = "training_logs_workout_details", constraint = """
                type <> 'WORKOUT' or (
                    workout_name_snapshot is not null and length(trim(workout_name_snapshot)) between 1 and 40
                    and duration_seconds is not null and duration_seconds >= 0
                    and notes is not null
                    and source_workout_id is not null
                )
                """)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class TrainingLog extends VersionedEntity {

    @Id
    private UUID id;

    protected TrainingLog(@NonNull UUID id, @NonNull User user,
                        @NonNull Instant startedAt, @NonNull Instant completedAt) {
        this.id = id;
        this.user = user;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    protected void setProgramSnapshot(@NonNull UUID programId, @NonNull UUID programItemId,
                                   @NonNull String programNameSnapshot, int itemPositionSnapshot) {
        this.programId = programId;
        this.programItemId = programItemId;
        this.programNameSnapshot = programNameSnapshot;
        this.itemPositionSnapshot = itemPositionSnapshot;
    }

    @Column(nullable = false, updatable = false)
    private Instant startedAt;

    @Column(nullable = false, updatable = false)
    private Instant completedAt;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private UUID programId;
    private UUID programItemId;
    private String programNameSnapshot;
    private Integer itemPositionSnapshot;
}
