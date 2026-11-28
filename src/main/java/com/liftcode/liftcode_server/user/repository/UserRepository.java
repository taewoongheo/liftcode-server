package com.liftcode.liftcode_server.user.repository;

import com.liftcode.liftcode_server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
