package com.liftcode.liftcode_server.setting.domain;

import com.liftcode.liftcode_server.user.domain.User;
import com.liftcode.liftcode_server.common.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "statistics_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StatisticsSetting extends VersionedEntity {

    @Id
    private UUID id;

    @MapsId
    @OneToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb",
            check = @CheckConstraint(constraint = "jsonb_typeof(favorites_json) = 'array'"))
    private String favoritesJson;

    public StatisticsSetting(
            @NonNull User user,
            String favoritesJson) {
        this.id = user.getId();
        this.user = user;
        this.favoritesJson = favoritesJson;
    }
}
