package com.liftcode.liftcode_server.sync.dto;

import java.util.List;
import java.util.UUID;

public record PushResult(
        UUID operationId,
        PushStatus status,
        Long sequence,
        List<ResourceVersion> resources) {}
