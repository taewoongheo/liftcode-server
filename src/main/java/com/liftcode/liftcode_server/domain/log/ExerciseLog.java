package com.liftcode.liftcode_server.domain.log;

import com.liftcode.liftcode_server.domain.OrderedItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "exercise_logs", uniqueConstraints = @UniqueConstraint(columnNames = {"training_log_id", "sort_key"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseLog extends OrderedItem {

    public ExerciseLog(
            UUID id,
            @NonNull WorkoutLog workoutLog,
            @NonNull String exerciseKey,
            String sortKey,
            @NonNull TrackingType trackingType,
            boolean supersetWithNext) {
        super(id, sortKey);
        this.workoutLog = workoutLog;
        this.exerciseKey = exerciseKey;
        this.trackingType = trackingType;
        this.supersetWithNext = supersetWithNext;
    }

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "training_log_id", nullable = false)
    private WorkoutLog workoutLog;

    @OneToMany(mappedBy = "exerciseLog", cascade = ALL)
    @OrderBy("sortKey ASC")
    private List<SetLog> setLogs = new ArrayList<>();

    @Column(nullable = false)
    private String exerciseKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrackingType trackingType;

    @Column(nullable = false)
    private boolean supersetWithNext;

    public enum TrackingType {
        REPS, WEIGHT_REPS, TIME, TIME_DISTANCE
    }
}
