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
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Entity
@Table(name = "measurement_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeasurementSetting extends VersionedEntity {

    @Id
    private UUID id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'KM'")
    private DistanceUnit distanceUnit;

    public MeasurementSetting(User user) {
        this(user, DistanceUnit.KM);
    }

    public MeasurementSetting(@NonNull User user, @NonNull DistanceUnit distanceUnit) {
        this.id = user.getId();
        this.user = user;
        this.distanceUnit = distanceUnit;
    }

    public enum DistanceUnit {
        KM, MI
    }
}
