package com.liftcode.liftcode_server.domain;

import com.liftcode.liftcode_server.domain.log.ExerciseLog;
import com.liftcode.liftcode_server.domain.log.SetLog;
import com.liftcode.liftcode_server.domain.log.WorkoutLog;
import com.liftcode.liftcode_server.domain.routine.Workout;
import com.liftcode.liftcode_server.domain.routine.WorkoutExercise;
import com.liftcode.liftcode_server.domain.routine.WorkoutSet;
import com.liftcode.liftcode_server.utils.FractionalIndex;
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
        var user = new User(UUID.randomUUID());
        em.persist(user);

        UUID workoutLogId = UUID.randomUUID();
        var workout = new WorkoutLog(workoutLogId, user, "Workout", Instant.EPOCH, Instant.EPOCH, 0, "");
        em.persist(workout);

        var exercise = new ExerciseLog(UUID.randomUUID(), workout, "squat", "a1", "reps", false);
        em.persist(exercise);

        var set = new SetLog(UUID.randomUUID(), exercise, "a1", "working", "kg");
        em.persist(set);
        var earlierExercise = new ExerciseLog(UUID.randomUUID(), workout, "bench", "a0", "reps", false);
        var earlierSet = new SetLog(UUID.randomUUID(), exercise, "a0", "warmup", "kg");
        em.persist(earlierExercise);
        em.persist(earlierSet);
        em.flush();
        em.clear();

        assertEquals(workoutLogId, workout.getId());
        var savedWorkout = em.find(WorkoutLog.class, workoutLogId);
        assertEquals(List.of(earlierExercise.getId(), exercise.getId()),
                savedWorkout.getExerciseLogs().stream().map(ExerciseLog::getId).toList());
        assertEquals(List.of(earlierSet.getId(), set.getId()),
                em.find(ExerciseLog.class, exercise.getId()).getSetLogs().stream().map(SetLog::getId).toList());
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

    @Test
    void routineItemsPersistTheirInitialAndChangedSortKeys() {
        var user = new User(UUID.randomUUID());
        var workout = new Workout(UUID.randomUUID(), user, "Leg day");
        String initial = FractionalIndex.between(null, null);
        var exercise = new WorkoutExercise(UUID.randomUUID(), workout, "squat", initial, false);
        var set = new WorkoutSet(UUID.randomUUID(), exercise, "working", initial);
        em.persist(user);
        em.persist(workout);
        em.persist(exercise);
        em.persist(set);
        em.flush();
        em.clear();

        var savedExercise = em.find(WorkoutExercise.class, exercise.getId());
        var savedSet = em.find(WorkoutSet.class, set.getId());
        assertEquals(initial, savedExercise.getSortKey());
        assertEquals(initial, savedSet.getSortKey());

        String next = FractionalIndex.between(initial, null);
        savedExercise.changeSortKey(next);
        savedSet.changeSortKey(next);
        em.flush();
        em.clear();

        var updatedExercise = em.find(WorkoutExercise.class, exercise.getId());
        var updatedSet = em.find(WorkoutSet.class, set.getId());
        assertEquals(next, updatedExercise.getSortKey());
        assertEquals(next, updatedSet.getSortKey());
        assertEquals(1, updatedExercise.getVersion());
        assertEquals(1, updatedSet.getVersion());
    }
}
