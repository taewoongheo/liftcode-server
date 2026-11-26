package com.liftcode.liftcode_server.workout.domain;

import com.liftcode.liftcode_server.user.domain.User;
import com.liftcode.liftcode_server.common.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Table(name = "exercise_notes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "exercise_key"}),
        check = @CheckConstraint(name = "exercise_note_sync_id",
                constraint = "id = md5(user_id::text || ':exercise_notes:' || exercise_key)::uuid"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseNote extends VersionedEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false, updatable = false, columnDefinition = "text",
            check = @CheckConstraint(constraint = "length(trim(exercise_key)) > 0"))
    private String exerciseKey;

    @Column(columnDefinition = "text", check = @CheckConstraint(constraint = "length(trim(note)) between 1 and 500"))
    private String note;

    public ExerciseNote(@NonNull UUID id, @NonNull User user, @NonNull String exerciseKey, String note) {
        this.id = id;
        this.user = user;
        this.exerciseKey = exerciseKey;
        this.note = note;
    }
}
