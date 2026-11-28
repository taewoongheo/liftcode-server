package com.liftcode.liftcode_server.program.domain;

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
@Table(name = "programs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Program extends VersionedEntity {

    @Id
    private UUID id;

    public Program(@NonNull UUID id, @NonNull User user, @NonNull String name, @NonNull Instant progressStartedAt) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.progressStartedAt = progressStartedAt;
    }

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, check = @CheckConstraint(constraint = "length(trim(name)) between 1 and 50"))
    private String name;

    @Column(name = "progress_started_at", nullable = false)
    private Instant progressStartedAt;
}
