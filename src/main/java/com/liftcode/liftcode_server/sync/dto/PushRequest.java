package com.liftcode.liftcode_server.sync.dto;

import java.util.List;
import java.util.UUID;

public record PushRequest(
        UUID deviceId,
        List<PushOperation> operations) {}
