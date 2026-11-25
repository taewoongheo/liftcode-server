package com.liftcode.liftcode_server.domain.sync;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TombstoneId(
        @Column(name = "user_id")UUID userId,
        @Column(name = "resource_id") UUID resourceId,
        @Column(name = "resource_type") String resourceType
) {}
