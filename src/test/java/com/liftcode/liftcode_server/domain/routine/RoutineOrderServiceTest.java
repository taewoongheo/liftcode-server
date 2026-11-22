package com.liftcode.liftcode_server.domain.routine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.persistence.OptimisticLockException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RoutineOrderServiceTest {
    @Autowired JdbcTemplate db;
    @Autowired RoutineOrderService order;

    private UUID workout() {
        UUID user = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        db.update("insert into users (id) values (?)", user);
        db.update("insert into workouts (id,user_id,name,version) values (?,?,'Test',0)", id, user);
        return id;
    }

    private UUID exercise(UUID workout, String rank, boolean superset) {
        UUID id = UUID.randomUUID();
        db.update("""
            insert into workout_exercises (id,workout_id,exercise_key,sort_key,superset_with_next,version)
            values (?,?,'squat',?,?,0)
            """, id, workout, rank, superset);
        return id;
    }

    private UUID set(UUID exercise, UUID parent, String rank) {
        UUID id = UUID.randomUUID();
        db.update("""
            insert into workout_sets (id,workout_exercise_id,parent_workout_set_id,set_type,sort_key,version)
            values (?,?,?,'drop',?,0)
            """, id, exercise, parent, rank);
        return id;
    }

    private List<UUID> sets(UUID exercise) {
        return db.queryForList("select id from workout_sets where workout_exercise_id=? order by sort_key", UUID.class, exercise);
    }

    @Test
    void movePreservesGroupsAndInvalidOrRedundantMovesDoNotAdvanceVersion() {
        UUID workout = workout();
        UUID ex = exercise(workout, "V0", false);
        UUID other = exercise(workout, "Z0", false);
        UUID a = set(ex, null, "10");
        UUID p = set(ex, null, "20");
        UUID child = set(ex, p, "30");
        UUID b = set(ex, null, "40");
        assertEquals(1, order.moveSet(ex, p, a, 0));
        assertEquals(List.of(p, child, a, b), sets(ex));
        assertEquals(1, order.moveSet(ex, p, a, 1));
        assertThrows(IllegalArgumentException.class, () -> order.moveSet(ex, a, child, 1));
        assertThrows(IllegalArgumentException.class, () -> order.moveSet(other, b, null, 0));
        assertEquals(2, order.moveSet(ex, a, null, 1));
        assertEquals(List.of(p, child, b, a), sets(ex));
        assertEquals(0, db.queryForObject("select version from workout_sets where id=?", Integer.class, b));
        assertEquals(0, db.queryForObject("select version from workouts where id=?", Integer.class, workout));
    }

    @Test
    void exerciseMoveDisconnectsOnlyBrokenSupersets() {
        UUID workout = workout();
        UUID a = exercise(workout, "10", true);
        UUID b = exercise(workout, "20", false);
        UUID c = exercise(workout, "30", true);
        UUID d = exercise(workout, "40", false);
        assertEquals(1, order.moveExercise(workout, b, null, 0));
        assertEquals(List.of(a,c,d,b), db.queryForList(
            "select id from workout_exercises where workout_id=? order by sort_key", UUID.class, workout));
        assertEquals(List.of(c), db.queryForList(
            "select id from workout_exercises where workout_id=? and superset_with_next", UUID.class, workout));
    }

    @Test
    void concurrentMovesIntoSameGapAreCheckedAgainstTheExerciseVersion() throws Exception {
        UUID workout = workout();
        UUID ex = exercise(workout, "10", false);
        UUID other = exercise(workout, "20", false);
        UUID a = set(ex, null, "10");
        UUID b = set(ex, null, "20");
        UUID c = set(ex, null, "30");
        UUID d = set(ex, null, "40");
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var futures = List.of(c, d).stream().map(id -> pool.submit((Callable<Boolean>) () -> {
                ready.countDown();
                start.await();
                try {
                    return order.moveSet(ex, id, b, 0) == 1;
                } catch (OptimisticLockException | OptimisticLockingFailureException expected) {
                    return false;
                }
            })).toList();
            boolean bothReady = ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            assertTrue(bothReady);
            assertNotEquals(futures.get(0).get(10, TimeUnit.SECONDS), futures.get(1).get(10, TimeUnit.SECONDS));
        }
        var sorted = sets(ex);
        assertEquals(a, sorted.getFirst());
        assertEquals(b, sorted.get(2));
        assertEquals(1, db.queryForObject("select version from workout_exercises where id=?", Integer.class, ex));
        UUID first = set(other, null, "10");
        set(other, null, "20");
        assertEquals(1, order.moveSet(other, first, null, 0));
    }
}
