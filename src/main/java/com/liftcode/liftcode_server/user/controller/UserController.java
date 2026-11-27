package com.liftcode.liftcode_server.user.controller;

import com.liftcode.liftcode_server.user.domain.User;
import com.liftcode.liftcode_server.user.dto.JoinUserRequest;
import com.liftcode.liftcode_server.user.dto.JoinUserResponse;
import com.liftcode.liftcode_server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<JoinUserResponse> join(@RequestBody JoinUserRequest joinUserRequest) {
        JoinUserResponse join = userService.join(joinUserRequest);
        return ResponseEntity.ok(join);
    }

}
