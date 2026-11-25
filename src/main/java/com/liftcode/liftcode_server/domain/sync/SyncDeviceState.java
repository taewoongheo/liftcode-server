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
@Table(name = "sync_device_states")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SyncDeviceState {

    @EmbeddedId
    private DeviceStateId deviceStateId;

    @MapsId("userId")
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    private Long lastPulledSequence;

    @Column(nullable = false)
    private Instant lastSeenAt;

    public SyncDeviceState(
            @NonNull DeviceStateId deviceStateId,
            @NonNull User user,
            Long lastPulledSequence,
            @NonNull Instant lastSeenAt) {
        this.deviceStateId = deviceStateId;
        this.user = user;
        this.lastPulledSequence = lastPulledSequence;
        this.lastSeenAt = lastSeenAt;
    }

    /**
     * TODO: 예외처리
     */
    public void markPulled(long sequence, @NonNull Instant now) {
        if (sequence < 0) throw new IllegalArgumentException("0 보다 작은 시퀀스 불가");
        if (this.lastPulledSequence != null && sequence < this.lastPulledSequence) throw new IllegalArgumentException("새 시퀀스는 기존 시퀀스보다 커야됨");
        this.lastPulledSequence = sequence;
        this.lastSeenAt = now;
    }
}
