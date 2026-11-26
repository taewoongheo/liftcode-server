package com.liftcode.liftcode_server.sync.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PushOperation(
        UUID operationId,
        Instant occurredAt,
        List<PushChange> changes) {}
