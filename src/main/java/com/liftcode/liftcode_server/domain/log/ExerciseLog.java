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
@Table(name = "exercise_logs", uniqueConstraints = @UniqueConstraint(columnNames = {"workout_log_id", "sort_key"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseLog extends OrderedItem {

    public ExerciseLog(
            UUID id,
            @NonNull WorkoutLog workoutLog,
            @NonNull String exerciseKey,
            String sortKey,
            @NonNull String trackingType,
            boolean supersetWithNext) {
        super(id, sortKey);
        this.workoutLog = workoutLog;
        this.exerciseKey = exerciseKey;
        this.trackingType = trackingType;
        this.supersetWithNext = supersetWithNext;
    }

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "workout_log_id", nullable = false)
    private WorkoutLog workoutLog;

    @OneToMany(mappedBy = "exerciseLog", cascade = ALL)
    @OrderBy("sortKey ASC")
    private List<SetLog> setLogs = new ArrayList<>();

    @Column(nullable = false)
    private String exerciseKey;

    @Column(nullable = false)
    private String trackingType;

    @Column(nullable = false)
    private boolean supersetWithNext;
}
