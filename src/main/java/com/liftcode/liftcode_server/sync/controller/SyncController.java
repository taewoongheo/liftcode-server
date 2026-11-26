package com.liftcode.liftcode_server.sync.controller;

import com.liftcode.liftcode_server.sync.dto.PushRequest;
import com.liftcode.liftcode_server.sync.dto.PushResponse;
import com.liftcode.liftcode_server.sync.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/sync/{userId}")
    public ResponseEntity<PushResponse> push(
            @RequestParam UUID userId,
            @RequestBody PushRequest request) {

        PushResponse response = syncService.push(userId, request);
        return ResponseEntity.ok(response);
    }
}
