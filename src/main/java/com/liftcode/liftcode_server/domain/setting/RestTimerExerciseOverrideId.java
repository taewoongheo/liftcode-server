package com.liftcode.liftcode_server.domain.setting;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.CheckConstraint;

import java.util.UUID;

@Embeddable
public record RestTimerExerciseOverrideId(
        @Column(name = "user_id") UUID userId,
        @Column(name = "exercise_id", nullable = false,
                check = @CheckConstraint(constraint = "length(trim(exercise_id)) > 0")) String exerciseId
) {}
