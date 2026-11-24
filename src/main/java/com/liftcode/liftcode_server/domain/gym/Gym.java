package com.liftcode.liftcode_server.domain.gym;

import com.liftcode.liftcode_server.domain.User;
import com.liftcode.liftcode_server.domain.VersionedEntity;
import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "gyms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gym extends VersionedEntity {

    @Id
    private UUID id;

    public Gym(@NonNull UUID id, @NonNull User user, @NonNull String name) {
        this.id = id;
        this.user = user;
        this.name = name;
    }

    @ManyToOne(fetch = LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20, check = @CheckConstraint(constraint = "length(trim(name)) > 0"))
    private String name;

    @ElementCollection
    @CollectionTable(name = "gym_exercises", joinColumns = @JoinColumn(name = "gym_id"),
            indexes = @Index(columnList = "exercise_id, gym_id"))
    @Column(name = "exercise_id", nullable = false, check = @CheckConstraint(constraint = "length(trim(exercise_id)) > 0"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<String> exerciseIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "gym_equipment", joinColumns = @JoinColumn(name = "gym_id"))
    @Column(name = "equipment_id", nullable = false, check = @CheckConstraint(constraint = "length(trim(equipment_id)) > 0"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<String> equipmentIds = new HashSet<>();
}
