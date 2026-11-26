package com.liftcode.liftcode_server.sync.domain;

import com.liftcode.liftcode_server.sync.domain.change.SyncResourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.UUID;

@Embeddable
public record TombstoneId(
        @Column(name = "user_id") UUID userId,
        @Enumerated(EnumType.STRING)
        @Column(name = "resource_type", nullable = false) SyncResourceType resourceType,
        @Column(name = "resource_id", nullable = false) UUID resourceId
) {}
