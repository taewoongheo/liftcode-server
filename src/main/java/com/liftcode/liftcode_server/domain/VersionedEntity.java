package com.liftcode.liftcode_server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@MappedSuperclass
public abstract class VersionedEntity {

    @Id
    private UUID id;

    @Version
    @ColumnDefault("0")
    @Column(nullable = false)
    private int version;

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void assignId(UUID id) {
        if (this.id != null) {
            throw new IllegalStateException("Entity ID has already been assigned");
        }
        this.id = Objects.requireNonNull(id, "id");
    }
}
