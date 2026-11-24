package com.liftcode.liftcode_server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class OrderedItem extends VersionedEntity {

    protected OrderedItem(@NonNull String sortKey) {
        this.sortKey = sortKey;
    }

    public void changeSortKey(@NonNull String sortKey) {
        this.sortKey = sortKey;
    }

    @Column(name = "sort_key", nullable = false, columnDefinition = "text collate \"C\"")
    private String sortKey;
}
