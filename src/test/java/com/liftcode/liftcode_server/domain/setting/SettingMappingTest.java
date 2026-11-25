package com.liftcode.liftcode_server.domain.setting;

import com.liftcode.liftcode_server.domain.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SettingMappingTest {
    @Autowired EntityManager em;

    @Test
    void settingsUseUserKeysAndPreserveNullVersusEmptyValues() {
        var first = new User(UUID.randomUUID());
        var second = new User(UUID.randomUUID());
        em.persist(first);
        em.persist(second);
        em.persist(new WorkoutSetting(first, true, false));
        em.persist(new StatisticsSetting(first, null));
        em.persist(new StatisticsSetting(second, "[]"));
        var category = new RestTimerCategoryOverride(UUID.randomUUID(), first, RestTimerCategory.CORE, null);
        var otherCategory = new RestTimerCategoryOverride(UUID.randomUUID(), second, RestTimerCategory.CORE, 60);
        var exercise = new RestTimerExerciseOverride(UUID.randomUUID(), first, "squat", 0);
        em.persist(category);
        em.persist(otherCategory);
        em.persist(exercise);
        em.flush();
        em.clear();

        var workoutSetting = em.find(WorkoutSetting.class, first.getId());
        assertEquals(first.getId(), workoutSetting.getUser().getId());
        assertTrue(workoutSetting.isRirTrackingEnabled());
        assertFalse(workoutSetting.isRestTimerEnabled());
        assertNull(em.find(StatisticsSetting.class, first.getId()).getFavoritesJson());
        assertEquals("[]", em.find(StatisticsSetting.class, second.getId()).getFavoritesJson());
        assertNull(em.find(RestTimerCategoryOverride.class, category.getId()).getDuration());
        assertEquals(60, em.find(RestTimerCategoryOverride.class, otherCategory.getId()).getDuration());
        assertEquals(0, em.find(RestTimerExerciseOverride.class, exercise.getId()).getDuration());

        em.createNativeQuery("delete from users where id = :id")
                .setParameter("id", first.getId()).executeUpdate();
        em.clear();
        assertNull(em.find(WorkoutSetting.class, first.getId()));
        assertNull(em.find(StatisticsSetting.class, first.getId()));
        assertNull(em.find(RestTimerCategoryOverride.class, category.getId()));
        assertNull(em.find(RestTimerExerciseOverride.class, exercise.getId()));
        assertNotNull(em.find(RestTimerCategoryOverride.class, otherCategory.getId()));
    }
}
