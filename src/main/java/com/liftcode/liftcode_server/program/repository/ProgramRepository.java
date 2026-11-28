package com.liftcode.liftcode_server.program.repository;

import com.liftcode.liftcode_server.program.domain.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProgramRepository extends JpaRepository<Program, UUID> {
}
