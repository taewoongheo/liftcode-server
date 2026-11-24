package com.liftcode.liftcode_server.domain.setting;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.UUID;

@Embeddable
public record RestTimerCategoryOverrideId(
        @Column(name = "user_id") UUID userId,
        @Enumerated(EnumType.STRING)
        @Column(name = "category", nullable = false) RestTimerCategory category
) {}
