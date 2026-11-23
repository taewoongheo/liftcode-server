package com.liftcode.liftcode_server.domain.log;

import com.liftcode.liftcode_server.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@DiscriminatorValue("REST")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestLog extends TrainingLog {

    public RestLog(UUID id, User user, Instant startedAt, Instant completedAt,
                   UUID programId, UUID programItemId, String programNameSnapshot, int itemPositionSnapshot) {
        super(id, user, startedAt, completedAt);
        setProgramSnapshot(programId, programItemId, programNameSnapshot, itemPositionSnapshot);
    }
}
