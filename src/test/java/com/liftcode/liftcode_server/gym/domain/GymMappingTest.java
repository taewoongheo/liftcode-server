package com.liftcode.liftcode_server.gym.domain;

import com.liftcode.liftcode_server.user.domain.User;
import com.liftcode.liftcode_server.common.domain.WeightUnit;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class GymMappingTest {
    @Autowired EntityManager em;

    @Test
    void persistsGymConfigurationAndVersionsDefaultClearingBeforeDeletion() {
        var user = new User(UUID.randomUUID());
        var gym = new Gym(UUID.randomUUID(), user, "Home");
        gym.getExerciseIds().add("squat");
        gym.getEquipmentIds().add("barbell");
        var weights = new GymEquipmentWeightSetting(UUID.randomUUID(), gym, "barbell", WeightMethod.BAR_AND_PLATES);
        var range = new WeightRange(10, 50, 2.5);
        weights.getRanges().put(0, range);
        weights.getPlateDenominations().add(1.25);
        weights.changeBarWeight(45.0);
        em.persist(user);
        em.persist(gym);
        user.setDefaultGym(gym);
        em.persist(weights);
        em.flush();
        em.clear();

        var savedGym = em.find(User.class, user.getId()).getDefaultGym();
        assertEquals(gym.getId(), savedGym.getId());
        assertTrue(savedGym.getExerciseIds().contains("squat"));
        assertTrue(savedGym.getEquipmentIds().contains("barbell"));
        var savedWeights = em.find(GymEquipmentWeightSetting.class, weights.getId());
        assertEquals(0, savedWeights.getVersion());
        assertEquals(WeightMethod.BAR_AND_PLATES, savedWeights.getActiveWeightMethod());
        assertEquals(45, savedWeights.getBarWeight());
        assertEquals(range, savedWeights.getRanges().get(0));
        assertEquals(WeightUnit.LB, savedWeights.getRangeUnit());
        assertEquals(WeightUnit.LB, savedWeights.getBarUnit());
        assertTrue(savedWeights.getPlateDenominations().contains(1.25));

        savedWeights.changeWeightMethod(WeightMethod.RANGE);
        em.flush();
        em.clear();
        var rangeSettings = em.find(GymEquipmentWeightSetting.class, weights.getId());
        assertEquals(1, rangeSettings.getVersion());
        assertEquals(WeightMethod.RANGE, rangeSettings.getActiveWeightMethod());
        assertEquals(45.0, rangeSettings.getBarWeight());

        rangeSettings.changeBarWeight(null);
        em.flush();
        em.clear();
        assertNull(em.find(GymEquipmentWeightSetting.class, weights.getId()).getBarWeight());

        var savedUser = em.find(User.class, user.getId());
        int previousVersion = savedUser.getVersion();
        savedUser.setDefaultGym(null);
        em.flush();
        em.createNativeQuery("delete from gyms where id = :id").setParameter("id", gym.getId()).executeUpdate();
        em.clear();
        assertNull(em.find(User.class, user.getId()).getDefaultGym());
        assertEquals(previousVersion + 1, em.find(User.class, user.getId()).getVersion());
        assertNull(em.find(GymEquipmentWeightSetting.class, weights.getId()));
    }
}
