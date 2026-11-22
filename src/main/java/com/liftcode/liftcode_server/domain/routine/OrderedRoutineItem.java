package com.liftcode.liftcode_server.domain.routine;

import com.liftcode.liftcode_server.domain.VersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class OrderedRoutineItem extends VersionedEntity {

    @Column(name = "sort_key", nullable = false, columnDefinition = "text collate \"C\"")
    private String sortKey;
}
