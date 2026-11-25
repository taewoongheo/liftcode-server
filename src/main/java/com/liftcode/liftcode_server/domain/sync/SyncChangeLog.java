package com.liftcode.liftcode_server.domain.sync;

import com.liftcode.liftcode_server.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Table(name = "sync_change_log", uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "operation_id"}),
        indexes = @Index(name = "sync_change_log_user_sequence_idx", columnList = "user_id, sequence"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SyncChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sequence;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    private UUID operationId;

    @Column(nullable = false)
    private UUID deviceId;

    @Column(nullable = false)
    private Instant occurredAt;

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private String changes;

    public SyncChangeLog(
            @NonNull User user,
            @NonNull UUID operationId,
            @NonNull UUID deviceId,
            @NonNull Instant occurredAt,
            String changes) {
        this.user = user;
        this.operationId = operationId;
        this.deviceId = deviceId;
        this.occurredAt = occurredAt;
//        this.changes = changes;
    }
}
