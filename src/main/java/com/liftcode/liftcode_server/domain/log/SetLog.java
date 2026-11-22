package com.liftcode.liftcode_server.domain.log;

import com.liftcode.liftcode_server.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "set_logs")
@Getter
public class SetLog extends VersionedEntity {

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "exercise_log_id", nullable = false)
    private ExerciseLog exerciseLog;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_set_log_id")
    private SetLog parentSetLog;

    @OneToMany(mappedBy = "parentSetLog", cascade = ALL)
    private List<SetLog> setLogs;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private String setType;

    private double actualWeight;

    private Integer actualReps;

    /*
    * Double -> Integer 타입으로 변경
    * TODO: 클라이언트 db 변경 필요
    */
    private Integer actualRir;

    private Integer actualDurationSeconds;

    private double actualDistance;

    @Column(nullable = false)
    private String actualWeightUnit;
}
