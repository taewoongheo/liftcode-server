# TODO

- [ ] Map `IllegalArgumentException` to 400 with a `@RestControllerAdvice`. Validation errors from sync push currently return 500.
- [ ] Reject an operation that changes the same resource more than once with 400. It currently fails on the primary key and returns 500.
- [ ] Lock sync pushes per user. Two concurrent pushes can both pass the conflict check, and one of them then fails on a primary key or unique constraint with 500.
