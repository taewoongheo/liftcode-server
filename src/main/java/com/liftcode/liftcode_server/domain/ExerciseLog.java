package com.liftcode.liftcode_server.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "exercise_logs")
@Getter
public class ExerciseLog {

    @Id
    @GeneratedValue
    private UUID id;

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
