package com.liftcode.liftcode_server.domain.log;

import com.liftcode.liftcode_server.domain.User;
import com.liftcode.liftcode_server.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "workout_logs")
@Getter
public class WorkoutLog extends VersionedEntity {

    /*
    * workoutId 값을 저장
    * 원본 workout 이 삭제되어도 다른 log 들과 같은 workout 임을 ㅅ식별하기 위함
    */
    private UUID sourceWorkoutId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "workoutLog", cascade = ALL)
    private List<ExerciseLog> exerciseLogs;

    @Column(nullable = false)
    private String workoutNameSnapshot;

    @Column(nullable = false)
    private int durationSeconds;

    @Column(nullable = false)
    private String notes;

    @Column(nullable = false)
    private Instant startedAt;

    @Column(nullable = false)
    private Instant completedAt;
}
