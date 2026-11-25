# Keeping the sync cursor safe

## How the change log is used

`sync_change_log` numbers every change to a user's account.

- **Push.** One transaction checks `operation_id` for a retry, compares each resource's `baseVersion`, applies the change, and inserts one change log row. That row receives the next `sequence`.
- **Pull.** A device sends `afterSequence`. The server returns change log rows with a greater `sequence` and the current state of each affected resource. It then stores the new cursor in `sync_device_states`.

The cursor means "this device has received every change up to this number." A late-arriving lower number breaks that promise without raising an error.

## Problem: allocation order differs from commit order

An identity value is assigned when the row is inserted, but other transactions see the row only after commit. A rollback does not return the number.

| Time | Phone push (A) | Laptop push (B) | Tablet pull |
|---|---|---|---|
| t1 | Insert, receives **42** | | |
| t2 | Still updating the workout | Insert, receives **43** | |
| t3 | | Commit | |
| t4 | | | Reads `sequence > 41` and sees only 43 |
| t5 | | | Stores cursor 43 |
| t6 | Commit, so 42 becomes visible | | |
| t7 | | | Reads `sequence > 43`, so 42 is never delivered |

Gaps from rollbacks are harmless because pull skips missing numbers. Only a gap that is filled later causes a lost change.

## Fix: lock each user's sync work

Each operation always requests the same lock mode:

- Pull requests a **shared** lock because it only reads the change log.
- Push requests an **exclusive** lock because it creates a new sequence.

Two operations can run together only when both hold shared locks.

| Pair | Result |
|---|---|
| pull + pull | Run together |
| pull + push | The later operation waits until the earlier one ends |
| push + push | Run one at a time |

The lock key is the user ID, so different users never block each other.

### Lock lifetime

Use transaction-scoped locks. Commit or rollback releases them automatically. If the server process stops, PostgreSQL closes the connection, rolls back the transaction, and releases the lock. A failed push therefore never leaves pull waiting indefinitely.

Pull waits longer only when a push transaction stays open, for example during slow work inside the transaction. Keep sync transactions limited to database work and add a timeout, such as `lock_timeout` or `@Transactional(timeout = ...)`. The client already retries failed sync requests.

### Implementation options

| Option | How | Trade-off |
|---|---|---|
| PostgreSQL advisory lock | `pg_advisory_xact_lock` for push and `pg_advisory_xact_lock_shared` for pull, keyed by user ID | Does not lock a table row. Requires PostgreSQL-specific native queries. |
| Lock the `users` row | `@Lock(PESSIMISTIC_WRITE)` (`FOR UPDATE`) for push and `@Lock(PESSIMISTIC_READ)` (`FOR SHARE`) for pull | Uses standard JPA. While pull runs, it also blocks other writes to that user row. |

Rules for either option:

1. Run each push and pull in one `@Transactional` method.
2. Acquire the lock before reading versions, checking `operation_id`, or inserting the change log row.
3. Do not mark pull as read-only because it updates `sync_device_states`.

## Why the sequence uses `IDENTITY`

Hibernate's `SEQUENCE` strategy defaults to `allocationSize = 50`. Each server instance reserves a block of numbers in memory.

1. Instance 1 reserves 1–50, and instance 2 reserves 51–100.
2. The laptop's push reaches instance 2, receives 51, and commits.
3. The tablet pulls and stores cursor 51.
4. The phone's push reaches instance 1, receives 2, and commits.
5. The tablet's next pull reads `sequence > 51`, so it never receives 2.

The lock cannot prevent this because the numbers were assigned out of time order. Use `IDENTITY`, or `SEQUENCE` with `allocationSize = 1`, so the database assigns each value when the row is inserted.

`IDENTITY` makes Hibernate insert immediately on `persist()` and disables JDBC batch inserts. This cost is acceptable because each push writes one change log row.
