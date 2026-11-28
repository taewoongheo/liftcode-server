package com.liftcode.liftcode_server.sync.repository;

import com.liftcode.liftcode_server.sync.domain.SyncChangeLog;
import com.liftcode.liftcode_server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SyncChangeLogRepository extends JpaRepository<SyncChangeLog, Long> {

    Optional<SyncChangeLog> findByUserAndOperationId(User user, UUID operationId);
}
