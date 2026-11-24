package com.liftcode.liftcode_server.domain.body;

import com.liftcode.liftcode_server.domain.User;
import com.liftcode.liftcode_server.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "measurement_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeasurementSetting extends VersionedEntity {

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
        super(user.getId());
        this.user = user;
        this.distanceUnit = distanceUnit;
    }

    public enum DistanceUnit {
        KM, MI
    }
}
