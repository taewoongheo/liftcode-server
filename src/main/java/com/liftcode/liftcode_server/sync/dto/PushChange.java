package com.liftcode.liftcode_server.sync.dto;

import com.liftcode.liftcode_server.sync.domain.change.SyncChangeKind;
import com.liftcode.liftcode_server.sync.domain.change.SyncResourceType;

import java.util.Map;
import java.util.UUID;

public record PushChange(
        SyncResourceType resourceType,
        UUID resourceId,
        SyncChangeKind changeKind,
        int baseVersion,
        Map<String, Object> values) {}
