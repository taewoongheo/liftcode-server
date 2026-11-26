package com.liftcode.liftcode_server.sync.dto;

import java.util.List;

public record PushResponse(
        List<PushResult> results) {}
