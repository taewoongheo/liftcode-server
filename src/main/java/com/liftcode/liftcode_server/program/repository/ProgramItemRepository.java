package com.liftcode.liftcode_server.program.repository;

import com.liftcode.liftcode_server.program.domain.ProgramItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProgramItemRepository extends JpaRepository<ProgramItem, UUID> {
}
