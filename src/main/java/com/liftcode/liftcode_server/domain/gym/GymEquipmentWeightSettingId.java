package com.liftcode.liftcode_server.domain.gym;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record GymEquipmentWeightSettingId(
        @Column(name = "gym_id") UUID gymId,
        @Column(name = "equipment_id", nullable = false,
                check = @CheckConstraint(constraint = "length(trim(equipment_id)) > 0")) String equipmentId
) {}
