package com.liftcode.liftcode_server.sync.domain.change;

import lombok.Getter;
import lombok.NonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * One row change that the server applied, stored in the change log and returned by pull.
 *
 * @param version the row version after the change
 * @param values  the fields the server applied, keyed by field name; null for a delete.
 *                A null field value clears that field.
 */
public record SyncChange(
        @NonNull SyncResourceType resourceType,
        @NonNull UUID resourceId,
        @NonNull SyncChangeKind changeKind,
        int version,
        Map<String, Object> values
) {

    public SyncChange {
        if (version < 0) {
            throw new IllegalArgumentException("Version must not be negative");
        }
        if (changeKind == SyncChangeKind.DELETE) {
            if (values != null) {
                throw new IllegalArgumentException("A delete must not carry values");
            }
        } else {
            if (values == null) {
                throw new IllegalArgumentException("A create or update must carry values");
            }
            values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
        }
    }
}
