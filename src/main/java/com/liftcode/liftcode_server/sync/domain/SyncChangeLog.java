package com.liftcode.liftcode_server.sync.domain;

import com.liftcode.liftcode_server.user.domain.User;
import com.liftcode.liftcode_server.sync.domain.change.SyncChange;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<SyncChange> changes;

    public SyncChangeLog(
            @NonNull User user,
            @NonNull UUID operationId,
            @NonNull UUID deviceId,
            @NonNull Instant occurredAt,
            @NonNull List<SyncChange> changes) {
        this.user = user;
        this.operationId = operationId;
        this.deviceId = deviceId;
        this.occurredAt = occurredAt;
        this.changes = List.copyOf(changes);
    }
}
