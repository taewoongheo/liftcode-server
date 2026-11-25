# Choosing between a primary key and a unique constraint

## What each one means

Both reject duplicate values, and PostgreSQL backs both with a unique index.

| | Primary key | Unique constraint |
|---|---|---|
| Meaning | The row's identity | A rule that a combination cannot repeat |
| Per table | One | Any number |
| Null | Not allowed | Allowed. Nulls do not count as duplicates. |
| Foreign keys | Usually reference it | Can reference it, but rarely do |
| JPA | Maps to `@Id` and drives `findById()` | Does not affect entity identity |

A composite primary key changes the structure of every referencing table. Each foreign key must carry all key columns, and JPA needs `@EmbeddedId` or `@IdClass` plus `@JoinColumns`. Stored values do not change, but every reference becomes heavier.

## Decision rule

Ask one question: **must the same combination always be the same thing?**

- **Yes.** The combination is the identity. Use a composite primary key when the values never change, few tables reference the row, and no key column can be null.
- **No.** Create a single-column key, such as a UUID or an identity column. Add a unique constraint to each combination that must not repeat.

An apartment unit works like the first case. "Building 101, unit 1203" names exactly one home. A person works like the second case. Two people can share a name and an address, and both can change, so a separate identifier is needed.

When unsure, choose a single-column key with unique constraints. Changing a unique constraint later is easy. Replacing a composite key means rebuilding every foreign key.

## Applied to LiftCode

| Table | Primary key | Unique constraint | Reason |
|---|---|---|---|
| `sync_change_log` | `sequence` | `(user_id, operation_id)` | Each row is an independent record ordered by sequence. Retry protection is a rule, not identity. |
| `workout_exercises` | `id` | `(workout_id, sort_key)` | Reordering changes `sort_key`, and sets reference the row. |
| `rest_timer_category_overrides` | `id` (deterministic UUID) | `(user_id, category)` | A user has exactly one override per category. The ID is derived from that pair so it works as a single-column sync resource ID. |
| `sync_device_states` | `(user_id, device_id)` | None | The row is the state of one device. |
| `sync_tombstones` | `(user_id, resource_type, resource_id)` | None | The row records that one resource was deleted. The key matches the sync protocol's `(resourceType, resourceId)` pair. |

### Why `operation_id` is scoped to the user

The client generates `operation_id`. A unique constraint on that column alone would let one account block another account's push by reusing its ID. Scoping the rule to `user_id` limits it to one account.

### Why settings use a combination as identity

Two offline devices can each edit the same category override.

- With random UUIDs, the devices create two different resources. The second push violates `(user_id, category)` and needs custom conflict handling.
- With the combination as identity, both devices address the same resource. The second push becomes a normal version conflict.

The client follows the same rule. `keyedSettingsId` in the LiftCode app hashes `userId:resourceType:key` into a stable UUID so every device produces the same ID. The server stores this UUID as the primary key and keeps `(user_id, category)` unique. Every synced row then has a single UUID resource ID, and the unique constraint still rejects a second ID for the same pair.

### Tombstones for resources with stable IDs

A resource with a random UUID is deleted once, so its tombstone is written once. A resource whose ID comes from a combination can be deleted, recreated, and deleted again with the same ID. If such a resource needs tombstones, the service must:

1. Remove the tombstone when the resource is recreated.
2. Update the existing tombstone, not insert a new one, on a later deletion.
