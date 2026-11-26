package com.liftcode.liftcode_server.warmup.domain;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record WarmupStep(
        @Column(nullable = false, check = @CheckConstraint(constraint = "weight_percentage between 0 and 100"))
        double weightPercentage,
        @Column(check = @CheckConstraint(constraint = "reps between 1 and 9999"))
        Integer reps) {

    public WarmupStep {
        if (!Double.isFinite(weightPercentage) || weightPercentage < 0 || weightPercentage > 100) {
            throw new IllegalArgumentException("Weight percentage must be between 0 and 100");
        }
        if (reps != null && (reps < 1 || reps > 9999)) {
            throw new IllegalArgumentException("Reps must be between 1 and 9999");
        }
    }
}
