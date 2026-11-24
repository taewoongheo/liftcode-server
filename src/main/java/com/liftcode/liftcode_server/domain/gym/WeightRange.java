package com.liftcode.liftcode_server.domain.gym;

import jakarta.persistence.*;

@Embeddable
public record WeightRange(
        @Column(name = "min_weight", nullable = false,
                check = @CheckConstraint(constraint = "min_weight >= 0")) double minWeight,
        @Column(name = "max_weight", nullable = false,
                check = @CheckConstraint(constraint = "max_weight >= min_weight and max_weight < 'Infinity'")) double maxWeight,
        @Column(nullable = false,
                check = @CheckConstraint(constraint = "increment > 0 and increment < 'Infinity'")) double increment
) {}
