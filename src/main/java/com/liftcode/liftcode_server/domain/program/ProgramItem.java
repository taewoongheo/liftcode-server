package com.liftcode.liftcode_server.domain.program;

import com.liftcode.liftcode_server.domain.OrderedItem;
import com.liftcode.liftcode_server.domain.workout.Workout;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "program_items", uniqueConstraints = @UniqueConstraint(columnNames = {"program_id", "sort_key"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProgramItem extends OrderedItem {

    public ProgramItem(UUID id, @NonNull Program program, String sortKey, Workout workout) {
        super(id, sortKey);
        this.program = program;
        this.workout = workout;
    }

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "workout_id")
    private Workout workout;
}
