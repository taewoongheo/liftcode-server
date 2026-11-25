# Saving entities with assigned IDs

**Status.** The project currently accepts this cost. Revisit it during performance work.

## Why the IDs are assigned

The client creates workouts offline and generates their UUIDs. Push sends the same IDs to the server, so the server must keep them. Database-generated IDs would break the link between local and server rows.

## How `save()` decides between `persist` and `merge`

Spring Data's `save()` calls `persist` for a new entity and `merge` otherwise. It treats an entity as new when:

- the entity has a `@Version` field with a wrapper type such as `Integer` or `Long`, and the version is null; or
- the entity has no `@Version` field, or the field has a primitive type such as `int`, and the ID is null.

`VersionedEntity` declares `private int version`, so Spring Data checks the ID. Every entity sets its UUID in the constructor, so `save()` always calls `merge`.

## What `merge` does

`merge` treats the object as a copy of a database row. Hibernate cannot tell whether an assigned ID and a primitive version of `0` belong to a new row or an existing one. It runs a `SELECT` for each row and inserts the row when the query returns nothing.

## Measurement

Saving one new workout with 2 exercises and 4 sets (7 rows) through `repository.save()` ran:

```
select workouts
select workout_exercises
select workout_sets
select workout_sets
select workout_exercises
select workout_sets
select workout_sets
insert × 7
```

Cascade merges every child, so the number of extra queries equals the number of rows in the aggregate. A workout with 30 sets runs more than 30 extra `SELECT` statements on creation.

The test also showed that `save()` returned a different instance and left the original unmanaged.

```java
Workout workout = new Workout(id, user, "Upper");
workoutRepository.save(workout);
workout.changeName("Lower");          // Not saved: the original is not managed

Workout saved = workoutRepository.save(workout);
saved.changeName("Lower");            // Saved
```

## Where it matters in sync

- **Update.** Push loads the entity to compare `baseVersion`. The loaded entity is already managed, so dirty checking writes the change without calling `save()`.
- **Create.** A new workout cascades to all exercises and sets, so the cost is highest here.

## Fix options

| Option | How | Trade-off |
|---|---|---|
| Implement `Persistable` | Add `isNew()` to `VersionedEntity`, for example `createdAt == null`, or a `@Transient` flag cleared in `@PostPersist` and `@PostLoad` | Fixes every entity in one place. Spring Data calls `persist`, so creation runs only `INSERT` statements and the original instance stays managed. |
| Call `em.persist()` for creation | Add a create method to the repository | Simple, but any direct `save()` call still uses `merge`. |

## Until it is fixed

Use the instance that `save()` returns. Changes made to the original object after `save()` are lost.
