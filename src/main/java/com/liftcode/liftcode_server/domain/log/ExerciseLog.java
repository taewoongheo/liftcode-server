package com.liftcode.liftcode_server.domain.log;

import com.liftcode.liftcode_server.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "exercise_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseLog extends VersionedEntity {

    public ExerciseLog(UUID id, WorkoutLog workoutLog, String exerciseKey, int position, String trackingType, boolean supersetWithNext) {
        super(id);
        this.workoutLog = Objects.requireNonNull(workoutLog, "workoutLog");
        this.exerciseKey = Objects.requireNonNull(exerciseKey, "exerciseKey");
        this.position = position;
        this.trackingType = Objects.requireNonNull(trackingType, "trackingType");
        this.supersetWithNext = supersetWithNext;
    }

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "workout_log_id", nullable = false)
    private WorkoutLog workoutLog;

    @OneToMany(mappedBy = "exerciseLog", cascade = ALL)
    private List<SetLog> setLogs;

    @Column(nullable = false)
    private String exerciseKey;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private String trackingType;

    @Column(nullable = false)
    private boolean supersetWithNext;
}
