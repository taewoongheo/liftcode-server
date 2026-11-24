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

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "set_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"exercise_log_id", "sort_key"}),
        check = @CheckConstraint(name = "set_logs_not_own_parent", constraint = "parent_set_log_id <> id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SetLog extends OrderedItem {

    public SetLog(
            UUID id,
            @NonNull ExerciseLog exerciseLog,
            String sortKey,
            @NonNull String setType,
            @NonNull String actualWeightUnit) {
        super(id, sortKey);
        this.exerciseLog = exerciseLog;
        this.setType = setType;
        this.actualWeightUnit = actualWeightUnit;
    }

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "exercise_log_id", nullable = false)
    private ExerciseLog exerciseLog;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_set_log_id")
    private SetLog parentSetLog;

    @OneToMany(mappedBy = "parentSetLog", cascade = ALL)
    @OrderBy("sortKey ASC")
    private List<SetLog> setLogs = new ArrayList<>();

    @Column(nullable = false)
    private String setType;

    private Double actualWeight;

    @Column(check = @CheckConstraint(constraint = "actual_reps between 1 and 9999"))
    private Integer actualReps;

    /*
    * Double -> Integer 타입으로 변경
    * TODO: 클라이언트 db 변경 필요
    */
    @Column(check = @CheckConstraint(constraint = "actual_rir between 0 and 5"))
    private Integer actualRir;

    private Integer actualDurationSeconds;

    private Double actualDistance;

    @Column(nullable = false, check = @CheckConstraint(constraint = "actual_weight_unit in ('kg', 'lb')"))
    private String actualWeightUnit;
}
