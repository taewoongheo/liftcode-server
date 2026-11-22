package com.liftcode.liftcode_server.domain;

import com.liftcode.liftcode_server.domain.log.ExerciseLog;
import com.liftcode.liftcode_server.domain.log.SetLog;
import com.liftcode.liftcode_server.domain.log.WorkoutLog;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@SpringBootTest
@Transactional
class VersionedEntityTest {
    @Autowired EntityManager em;

    @Test
    void persistsUsersAndLogsWithAssignedIdsAndSharedVersionFields() {
        var user = new User();
        user.assignId(UUID.randomUUID());
        em.persist(user);

        UUID workoutLogId = UUID.randomUUID();
        var workout = new WorkoutLog();
        workout.assignId(workoutLogId);
        setField(workout, "user", user);
        setField(workout, "workoutNameSnapshot", "Workout");
        setField(workout, "notes", "");
        setField(workout, "startedAt", Instant.EPOCH);
        setField(workout, "completedAt", Instant.EPOCH);
        em.persist(workout);

        var exercise = new ExerciseLog();
        exercise.assignId(UUID.randomUUID());
        setField(exercise, "workoutLog", workout);
        setField(exercise, "exerciseKey", "squat");
        setField(exercise, "trackingType", "reps");
        em.persist(exercise);

        var set = new SetLog();
        set.assignId(UUID.randomUUID());
        setField(set, "exerciseLog", exercise);
        setField(set, "setType", "normal");
        setField(set, "actualWeightUnit", "kg");
        em.persist(set);
        em.flush();
        em.clear();

        assertEquals(workoutLogId, workout.getId());
        var savedWorkout = em.find(WorkoutLog.class, workoutLogId);
        for (VersionedEntity saved : List.of(savedWorkout, em.find(User.class, user.getId()),
                em.find(ExerciseLog.class, exercise.getId()), em.find(SetLog.class, set.getId()))) {
            assertNotNull(saved.getCreatedAt());
            assertNotNull(saved.getUpdatedAt());
            assertEquals(0, saved.getVersion());
        }
        setField(savedWorkout, "notes", "Done");
        em.flush();
        assertEquals(1, savedWorkout.getVersion());
    }
}
