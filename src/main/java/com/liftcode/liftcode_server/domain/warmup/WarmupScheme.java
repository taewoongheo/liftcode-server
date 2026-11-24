package com.liftcode.liftcode_server.domain.warmup;

import com.liftcode.liftcode_server.domain.User;
import com.liftcode.liftcode_server.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "warmup_schemes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "exercise_key"}),
        check = @CheckConstraint(name = "warmup_default_uses_user_id", constraint = "exercise_key is not null or id = user_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class WarmupScheme extends VersionedEntity {

    @Id
    private UUID id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Getter
    @Column(updatable = false, check = @CheckConstraint(constraint = "length(trim(exercise_key)) > 0"))
    private String exerciseKey;

    @ElementCollection
    @CollectionTable(name = "warmup_steps", joinColumns = @JoinColumn(name = "scheme_id"))
    @OrderColumn(name = "position", nullable = false, columnDefinition = "integer check (position >= 0)")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<WarmupStep> steps = new ArrayList<>();

    private WarmupScheme(@NonNull UUID id, @NonNull User user, String exerciseKey, List<WarmupStep> steps) {
        this.id = id;
        this.user = user;
        this.exerciseKey = exerciseKey;
        replaceSteps(steps);
    }

    public static WarmupScheme userDefault(@NonNull User user, List<WarmupStep> steps) {
        return new WarmupScheme(user.getId(), user, null, steps);
    }

    public static WarmupScheme forExercise(UUID id, User user, @NonNull String exerciseKey, List<WarmupStep> steps) {
        if (exerciseKey.isBlank()) {
            throw new IllegalArgumentException("Exercise key must not be blank");
        }
        return new WarmupScheme(id, user, exerciseKey, steps);
    }

    public List<WarmupStep> getSteps() {
        return List.copyOf(steps);
    }

    public void replaceSteps(List<WarmupStep> steps) {
        var replacement = List.copyOf(steps);
        if (replacement.isEmpty()) {
            throw new IllegalArgumentException("A warmup scheme requires at least one step");
        }
        this.steps.clear();
        this.steps.addAll(replacement);
    }
}
