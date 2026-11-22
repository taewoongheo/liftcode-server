package com.liftcode.liftcode_server.domain.routine;

import com.liftcode.liftcode_server.domain.VersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.UUID;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class OrderedRoutineItem extends VersionedEntity {

    protected OrderedRoutineItem(UUID id, @NonNull String sortKey) {
        super(id);
        this.sortKey = sortKey;
    }

    public void changeSortKey(@NonNull String sortKey) {
        this.sortKey = sortKey;
    }

    @Column(name = "sort_key", nullable = false, columnDefinition = "text collate \"C\"")
    private String sortKey;
}
