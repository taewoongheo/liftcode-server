package com.liftcode.liftcode_server.common.domain;

import com.liftcode.liftcode_server.log.domain.ExerciseLog;
import com.liftcode.liftcode_server.program.domain.Program;
import com.liftcode.liftcode_server.program.domain.ProgramItem;
import com.liftcode.liftcode_server.log.domain.SetLog;
import com.liftcode.liftcode_server.log.domain.TrainingLog;
import com.liftcode.liftcode_server.log.domain.WorkoutLog;
import com.liftcode.liftcode_server.log.domain.RestLog;
import com.liftcode.liftcode_server.workout.domain.Workout;
import com.liftcode.liftcode_server.workout.domain.SetType;
import com.liftcode.liftcode_server.workout.domain.WorkoutExercise;
import com.liftcode.liftcode_server.workout.domain.WorkoutSet;
import com.liftcode.liftcode_server.common.util.FractionalIndex;
import com.liftcode.liftcode_server.user.domain.User;
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
        UUID sourceWorkoutId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();
        UUID programItemId = UUID.randomUUID();
        Instant startedAt = Instant.parse("2026-01-10T09:00:00Z");
        Instant completedAt = startedAt.plusSeconds(600);
        var workout = new WorkoutLog(workoutLogId, user, startedAt, completedAt, sourceWorkoutId,
                "Workout", 600, "", programId, programItemId, "Strength", 3);
        em.persist(workout);

        var exercise = new ExerciseLog(UUID.randomUUID(), workout, "squat", "a1", ExerciseLog.TrackingType.REPS, false);
        em.persist(exercise);

        var set = new SetLog(UUID.randomUUID(), exercise, "a1", SetType.WORKING, WeightUnit.KG);
        em.persist(set);
        var earlierExercise = new ExerciseLog(UUID.randomUUID(), workout, "bench", "a0", ExerciseLog.TrackingType.REPS, false);
        var earlierSet = new SetLog(UUID.randomUUID(), exercise, "a0", SetType.WARMUP, WeightUnit.KG);
        em.persist(earlierExercise);
        em.persist(earlierSet);
        em.flush();
        em.clear();

        var savedWorkout = assertInstanceOf(WorkoutLog.class, em.find(TrainingLog.class, workoutLogId));
        assertEquals(sourceWorkoutId, savedWorkout.getSourceWorkoutId());
        assertEquals(programId, savedWorkout.getProgramId());
        assertEquals(programItemId, savedWorkout.getProgramItemId());
        assertEquals("Strength", savedWorkout.getProgramNameSnapshot());
        assertEquals(3, savedWorkout.getItemPositionSnapshot());
        assertEquals(startedAt, savedWorkout.getStartedAt());
        assertEquals(completedAt, savedWorkout.getCompletedAt());
        assertEquals(user.getId(), savedWorkout.getUser().getId());
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
    void databaseRejectsWorkoutHistoryWithoutSourceWorkoutId() {
        var user = new User(UUID.randomUUID());
        em.persist(user);
        em.flush();

        var failure = assertThrows(org.hibernate.exception.ConstraintViolationException.class, () ->
                em.createNativeQuery("""
                        insert into training_logs
                            (id, user_id, type, version, created_at, updated_at, started_at, completed_at,
                             workout_name_snapshot, duration_seconds, notes, source_workout_id)
                        values (:id, :userId, 'WORKOUT', 0, now(), now(), now(), now(), 'Workout', 0, '', null)
                        """)
                        .setParameter("id", UUID.randomUUID())
                        .setParameter("userId", user.getId())
                        .executeUpdate());
        assertEquals("training_logs_workout_details", failure.getConstraintName());
    }

    @Test
    void savesStandaloneWorkoutWithoutProgramSnapshot() {
        var user = new User(UUID.randomUUID());
        Instant start = Instant.parse("2026-01-10T09:00:00Z");
        UUID sourceId = UUID.randomUUID();
        var workout = new WorkoutLog(UUID.randomUUID(), user, start, start.plusSeconds(60),
                sourceId, "Workout", 60, "");
        em.persist(user);
        em.persist(workout);
        em.flush();
        em.clear();

        var saved = assertInstanceOf(WorkoutLog.class, em.find(TrainingLog.class, workout.getId()));
        assertNull(saved.getProgramId());
        assertNull(saved.getProgramItemId());
        assertNull(saved.getProgramNameSnapshot());
        assertNull(saved.getItemPositionSnapshot());
        assertEquals(sourceId, saved.getSourceWorkoutId());
        assertEquals(start, saved.getStartedAt());
        assertEquals(start.plusSeconds(60), saved.getCompletedAt());
    }

    @Test
    void savesProgramRestWithoutSourceWorkout() {
        var user = new User(UUID.randomUUID());
        UUID programId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        Instant start = Instant.parse("2026-01-11T00:00:00Z");
        Instant end = start.plusSeconds(3600);
        var program = new Program(programId, user, "Strength");
        var restDay = new ProgramItem(itemId, program, "a0", null);
        var rest = new RestLog(UUID.randomUUID(), user, start, end, programId, itemId, "Strength", 4);
        em.persist(user);
        em.persist(program);
        em.persist(restDay);
        em.persist(rest);
        em.flush();
        em.clear();

        assertNull(em.find(ProgramItem.class, itemId).getWorkout());
        var saved = assertInstanceOf(RestLog.class, em.find(TrainingLog.class, rest.getId()));
        assertEquals(user.getId(), saved.getUser().getId());
        assertEquals(programId, saved.getProgramId());
        assertEquals(itemId, saved.getProgramItemId());
        assertEquals("Strength", saved.getProgramNameSnapshot());
        assertEquals(4, saved.getItemPositionSnapshot());
        assertEquals(start, saved.getStartedAt());
        assertEquals(end, saved.getCompletedAt());
    }

    @Test
    void routineItemsPersistTheirInitialAndChangedSortKeys() {
        var user = new User(UUID.randomUUID());
        var workout = new Workout(UUID.randomUUID(), user, "Leg day");
        String initial = FractionalIndex.between(null, null);
        var exercise = new WorkoutExercise(UUID.randomUUID(), workout, "squat", initial, false);
        var set = new WorkoutSet(UUID.randomUUID(), exercise, SetType.WORKING, initial);
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
