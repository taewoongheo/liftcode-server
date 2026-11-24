package com.liftcode.liftcode_server.domain;

import com.liftcode.liftcode_server.domain.log.RestLog;
import com.liftcode.liftcode_server.domain.program.Program;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProgramProgressMappingTest {
    @Autowired EntityManager em;

    @Test
    void restartPersistsNewBoundaryAndVersionWithoutRemovingHistory() {
        var user = new User(UUID.randomUUID());
        var program = new Program(UUID.randomUUID(), user, "Strength");
        em.persist(user);
        em.persist(program);
        em.flush();
        em.clear();

        var saved = em.find(Program.class, program.getId());
        assertNull(saved.getProgressStartedAt());
        var firstStart = Instant.parse("2026-11-20T09:00:00Z");
        saved.startProgress(firstStart);
        var history = new RestLog(UUID.randomUUID(), em.find(User.class, user.getId()),
                firstStart, firstStart.plusSeconds(3600), saved.getId(), UUID.randomUUID(), "Strength", 1);
        em.persist(history);
        em.flush();
        em.clear();

        saved = em.find(Program.class, program.getId());
        assertEquals(firstStart, saved.getProgressStartedAt());
        int version = saved.getVersion();
        var restartedAt = Instant.parse("2026-11-24T09:00:00Z");
        saved.startProgress(restartedAt);
        em.flush();
        em.clear();

        saved = em.find(Program.class, program.getId());
        assertEquals(restartedAt, saved.getProgressStartedAt());
        assertEquals(version + 1, saved.getVersion());
        assertEquals(firstStart, em.find(RestLog.class, history.getId()).getStartedAt());
    }
}
