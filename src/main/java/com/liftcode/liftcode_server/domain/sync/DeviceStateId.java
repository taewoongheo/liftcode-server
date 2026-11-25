package com.liftcode.liftcode_server.domain.sync;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record DeviceStateId(
        @Column(name = "user_id")UUID userId,
        @Column(name = "device_id") UUID deviceId
) {}
