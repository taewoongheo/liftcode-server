package com.liftcode.liftcode_server.user.service;

import com.liftcode.liftcode_server.user.domain.User;
import com.liftcode.liftcode_server.user.dto.JoinUserRequest;
import com.liftcode.liftcode_server.user.dto.JoinUserResponse;
import com.liftcode.liftcode_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public JoinUserResponse join(JoinUserRequest joinUserRequest) {
        User savedUser = userRepository.save(new User(joinUserRequest.name()));
        return new JoinUserResponse(savedUser.getId(), savedUser.getName());
    }
}
