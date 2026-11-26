package com.liftcode.liftcode_server.sync.dto;

import com.liftcode.liftcode_server.sync.domain.change.SyncResourceType;

import java.util.UUID;

public record ResourceVersion(
        SyncResourceType resourceType,
        UUID resourceId,
        int version) {}
