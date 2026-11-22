package com.liftcode.liftcode_server.domain.routine;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;

import java.util.UUID;

@Getter
@MappedSuperclass
public abstract class RankedRoutineItem {
    @Id
    private UUID id;

    @Version
    private int version;

    @Column(name = "sort_key", nullable = false, columnDefinition = "text collate \"C\"")
    private String sortKey;

    void placeBetween(String previous, String next) {
        sortKey = FractionalIndex.between(previous, next);
    }
}
