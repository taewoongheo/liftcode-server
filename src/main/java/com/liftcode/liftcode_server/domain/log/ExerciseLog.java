package com.liftcode.liftcode_server.domain.log;

import com.liftcode.liftcode_server.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "exercise_logs")
@Getter
public class ExerciseLog extends VersionedEntity {

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
