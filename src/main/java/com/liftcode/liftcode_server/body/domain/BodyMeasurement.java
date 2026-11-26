package com.liftcode.liftcode_server.body.domain;

import com.liftcode.liftcode_server.user.domain.User;
import com.liftcode.liftcode_server.common.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "body_measurements",
        indexes = @Index(columnList = "user_id, measured_on, measured_at"),
        check = @CheckConstraint(name = "body_measurement_has_value",
                constraint = "weight_kg is not null or body_fat_percentage is not null or skeletal_muscle_mass_kg is not null"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BodyMeasurement extends VersionedEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    private Instant measuredAt;

    @Column(nullable = false)
    private LocalDate measuredOn;

    @Column(check = @CheckConstraint(constraint = "weight_kg > 0 and weight_kg <= 1000"))
    private Double weightKg;

    @Column(check = @CheckConstraint(constraint = "body_fat_percentage between 0 and 100"))
    private Double bodyFatPercentage;

    @Column(check = @CheckConstraint(constraint = "skeletal_muscle_mass_kg > 0 and skeletal_muscle_mass_kg <= 1000"))
    private Double skeletalMuscleMassKg;

    public BodyMeasurement(@NonNull UUID id, @NonNull User user, @NonNull Instant measuredAt, @NonNull LocalDate measuredOn,
                           Double weightKg, Double bodyFatPercentage, Double skeletalMuscleMassKg) {
        this.id = id;
        this.user = user;
        this.measuredAt = measuredAt;
        this.measuredOn = measuredOn;
        this.weightKg = weightKg;
        this.bodyFatPercentage = bodyFatPercentage;
        this.skeletalMuscleMassKg = skeletalMuscleMassKg;
    }
}
