package com.liftcode.liftcode_server.setting.domain;

import com.liftcode.liftcode_server.user.domain.User;
import com.liftcode.liftcode_server.common.domain.VersionedEntity;
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
@Table(name = "rest_timer_category_overrides", uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "category"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestTimerCategoryOverride extends VersionedEntity {

    @Id
    private UUID id;

    public RestTimerCategoryOverride(
            @NonNull UUID id,
            @NonNull User user,
            @NonNull RestTimerCategory category,
            Integer duration) {
        this.id = id;
        this.user = user;
        this.category = category;
        this.duration = duration;
    }

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestTimerCategory category;

    @Column(check = @CheckConstraint(constraint = "duration >= 0"))
    private Integer duration;
}
