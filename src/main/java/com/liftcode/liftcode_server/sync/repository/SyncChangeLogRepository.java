package com.liftcode.liftcode_server.sync.repository;

import com.liftcode.liftcode_server.sync.domain.SyncChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncChangeLogRepository extends JpaRepository<SyncChangeLog, Long> {
}
