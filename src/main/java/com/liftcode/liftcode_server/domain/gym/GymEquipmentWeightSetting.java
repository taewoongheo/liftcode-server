package com.liftcode.liftcode_server.domain.gym;

import com.liftcode.liftcode_server.domain.VersionedEntity;
import com.liftcode.liftcode_server.domain.WeightUnit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.*;

@Entity
@Table(name = "gym_equipment_weight_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GymEquipmentWeightSetting extends VersionedEntity {

    private static final WeightUnit DEFAULT_UNIT = WeightUnit.LB;

    public GymEquipmentWeightSetting(@NonNull Gym gym, @NonNull String equipmentId, @NonNull WeightMethod activeWeightMethod) {
        this.gym = gym;
        this.id = new GymEquipmentWeightSettingId(gym.getId(), equipmentId);
        this.activeWeightMethod = activeWeightMethod;
    }

    @EmbeddedId
    private GymEquipmentWeightSettingId id;

    @MapsId("gymId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gym_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Gym gym;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeightMethod activeWeightMethod;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'LB'")
    @Column(nullable = false)
    private WeightUnit rangeUnit = DEFAULT_UNIT;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'LB'")
    @Column(nullable = false)
    private WeightUnit barUnit = DEFAULT_UNIT;

    @Column(check = @CheckConstraint(constraint = "bar_weight >= 0 and bar_weight < 'Infinity'"))
    private Double barWeight;

    public void changeBarWeight(Double barWeight) {
        this.barWeight = barWeight;
    }

    public void changeWeightMethod(@NonNull WeightMethod method) {
        this.activeWeightMethod = method;
    }

    @ElementCollection
    @CollectionTable(name = "gym_equipment_weight_ranges", joinColumns = {
            @JoinColumn(name = "gym_id", referencedColumnName = "gym_id"),
            @JoinColumn(name = "equipment_id", referencedColumnName = "equipment_id")})
    @MapKeyColumn(name = "position", columnDefinition = "integer check (position >= 0)")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Map<Integer, WeightRange> ranges = new TreeMap<>();

    @ElementCollection
    @CollectionTable(name = "gym_plate_denominations", joinColumns = {
            @JoinColumn(name = "gym_id", referencedColumnName = "gym_id"),
            @JoinColumn(name = "equipment_id", referencedColumnName = "equipment_id")})
    @Column(name = "weight", nullable = false,
            check = @CheckConstraint(constraint = "weight > 0 and weight < 'Infinity'"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Double> plateDenominations = new HashSet<>();
}
