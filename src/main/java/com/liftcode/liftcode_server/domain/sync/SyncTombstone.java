package com.liftcode.liftcode_server.domain.sync;

import com.liftcode.liftcode_server.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Table(name = "sync_tombstones")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SyncTombstone {

    @EmbeddedId
    private TombstoneId tombstoneId;

    @MapsId("userId")
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private Long deletedSequence;

    @Column(nullable = false)
    private Instant deletedAt;

    public SyncTombstone(
            @NonNull TombstoneId tombstoneId,
            @NonNull User user,
            @NonNull Integer version,
            @NonNull Long deletedSequence,
            @NonNull Instant deletedAt) {
        this.tombstoneId = tombstoneId;
        this.user = user;
        this.version = version;
        this.deletedSequence = deletedSequence;
        this.deletedAt = deletedAt;
    }
}
