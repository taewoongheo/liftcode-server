package com.liftcode.liftcode_server.goal.domain;

import com.liftcode.liftcode_server.user.domain.User;
import com.liftcode.liftcode_server.common.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "goals", indexes = @Index(columnList = "user_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Goal extends VersionedEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb", check = @CheckConstraint(constraint = """
            jsonb_typeof(goal_json) = 'object'
            and jsonb_typeof(goal_json->'conditions') is not distinct from 'array'
            and jsonb_array_length(goal_json->'conditions') between 1 and 20
            """))
    private String goalJson;

    public Goal(@NonNull UUID id, @NonNull User user, @NonNull String goalJson) {
        this.id = id;
        this.user = user;
        this.goalJson = goalJson;
    }
}
