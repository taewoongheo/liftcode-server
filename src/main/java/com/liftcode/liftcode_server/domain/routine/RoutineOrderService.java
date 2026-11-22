package com.liftcode.liftcode_server.domain.routine;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class RoutineOrderService {
    private final EntityManager em;

    public RoutineOrderService(EntityManager em) {
        this.em = em;
    }

    /** Move before the given exercise, or to the end if beforeId is null. Returns the workout version. */
    public int moveExercise(UUID workoutId, UUID exerciseId, UUID beforeId, int expectedVersion) {
        Workout workout = lockOwner(Workout.class, workoutId);
        requireVersion(workout.getVersion(), expectedVersion);
        var original = em.createQuery("""
                select e from WorkoutExercise e where e.workout = :owner order by e.sortKey
                """, WorkoutExercise.class).setParameter("owner", workout).getResultList();
        var ordered = new ArrayList<>(original);
        int from = indexOf(ordered, exerciseId);
        int before = beforeId == null ? ordered.size() : indexOf(ordered, beforeId);
        if (!move(ordered, from, from + 1, before)) return expectedVersion;

        // Moving one exercise may break an existing superset with its former neighbour.
        var partners = new HashMap<UUID, WorkoutExercise>();
        for (int i = 0; i < ordered.size(); i++) {
            partners.put(ordered.get(i).getId(), next(ordered, i));
        }
        for (int i = 0; i < original.size(); i++) {
            WorkoutExercise exercise = original.get(i);
            if (next(original, i) != partners.get(exercise.getId())) {
                exercise.disconnectSuperset();
            }
        }
        em.lock(workout, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
        return workout.getVersion();
    }

    /** Move before a sibling, or to the end of its sibling group if beforeId is null. Returns the exercise version. */
    public int moveSet(UUID exerciseId, UUID setId, UUID beforeId, int expectedVersion) {
        WorkoutExercise exercise = lockOwner(WorkoutExercise.class, exerciseId);
        requireVersion(exercise.getVersion(), expectedVersion);
        var ordered = new ArrayList<>(em.createQuery("""
                select s from WorkoutSet s left join fetch s.parentWorkoutSet where s.workoutExercise = :owner order by s.sortKey
                """, WorkoutSet.class).setParameter("owner", exercise).getResultList());
        int from = indexOf(ordered, setId);
        UUID parent = ordered.get(from).parentId();
        int end = from + 1;
        if (parent == null) {
            // A drop/myo parent and its children move as a contiguous group.
            while (end < ordered.size() && setId.equals(ordered.get(end).parentId())) end++;
        }
        int before;
        if (beforeId != null) {
            before = indexOf(ordered, beforeId);
            if (!Objects.equals(parent, ordered.get(before).parentId())) {
                throw new IllegalArgumentException("Destination must be a sibling of the moved set");
            }
        } else if (parent != null) {
            before = end;
            while (before < ordered.size() && parent.equals(ordered.get(before).parentId())) before++;
        } else {
            before = ordered.size();
        }
        if (!move(ordered, from, end, before)) return expectedVersion;
        em.lock(exercise, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
        return exercise.getVersion();
    }

    private <T> T lockOwner(Class<T> type, UUID id) {
        T owner = em.find(type, id, LockModeType.PESSIMISTIC_WRITE);
        if (owner == null) throw new EntityNotFoundException(type.getSimpleName() + " " + id);
        return owner;
    }

    private static void requireVersion(int actual, int expected) {
        if (actual != expected) throw new OptimisticLockException("Expected version " + expected + ", found " + actual);
    }

    private static <T extends RankedRoutineItem> boolean move(List<T> items, int from, int end, int before) {
        if (from <= before && before <= end) return false;
        var moved = new ArrayList<>(items.subList(from, end));
        items.subList(from, end).clear();
        int to = before < from ? before : before - moved.size();
        String left = to == 0 ? null : items.get(to - 1).getSortKey();
        String right = to == items.size() ? null : items.get(to).getSortKey();
        for (T item : moved) {
            item.placeBetween(left, right);
            left = item.getSortKey();
        }
        items.addAll(to, moved);
        return true;
    }

    private static int indexOf(List<? extends RankedRoutineItem> items, UUID id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(id)) return i;
        }
        throw new IllegalArgumentException("Item does not belong to this list: " + id);
    }

    private static <T> T next(List<T> items, int index) {
        return index + 1 < items.size() ? items.get(index + 1) : null;
    }
}
