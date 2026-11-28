# TODO

## 1. 검증 실패를 400 으로 응답한다

- [ ] `@RestControllerAdvice` 에서 `IllegalArgumentException` 을 400 으로 매핑한다.

현재 예외 핸들러가 없어서 서비스가 던진 `IllegalArgumentException` 이 500 으로 나간다. 클라이언트는 요청이 잘못된 것인지 서버가 고장 난 것인지 구분할 수 없다.

이 예외를 던지는 곳:

- `SyncValuesReader.read`: values 에 명세에 없는 키가 있거나, 타입이 틀리거나, 필수 값이 없거나 null 일 때
- `SyncService.readCreations`: 생성 요청에 `baseVersion` 이 있을 때
- `SyncService.findConflicts`: 다른 사용자의 리소스 ID 로 생성할 때

같이 정할 것:

- 응답 본문 형식. JSON 이 깨졌거나 enum 값이 틀린 요청은 Spring 이 이미 `HttpMessageNotReadableException` 으로 400 을 준다. 두 경우의 본문을 맞출지 정한다.
- 사용자가 없을 때 던지는 `IllegalStateException` 은 400 이 아니다. 인증을 붙일 때 401 과 404 중 무엇으로 줄지 정한다.
- `readCreations` 의 메시지 "잘못된 요청입니다" 는 어떤 값이 문제인지 알려주지 않는다.
- 검증에 실패하면 트랜잭션 전체가 롤백된다. 같은 요청에서 앞서 APPLIED 된 operation 도 취소된다. Supabase 구현도 같았다.

## 2. 한 operation 에서 같은 리소스를 두 번 바꾸면 400 으로 거부한다

- [ ] DB 를 조회하기 전에 `(resourceType, resourceId)` 중복을 검사해 `IllegalArgumentException` 을 던진다.

현재 같은 `resourceId` 로 CREATE 가 두 번 오면 DB 에 행이 없으므로 `findConflicts` 를 통과한다. `apply` 의 두 번째 `save` 에서 같은 ID 의 엔티티가 이미 영속성 컨텍스트에 있어 예외가 나고 500 이 된다.

- 거부하는 이유: 뒤의 변경이 같은 operation 안에서 앞의 변경이 만든 버전을 `baseVersion` 으로 가리키게 되어 충돌 검사가 복잡해진다. 결과 `resources` 에 같은 리소스가 두 번 담기는 문제도 있다. 클라이언트가 한 리소스의 변경을 하나로 합쳐 보내야 한다.
- 범위: CREATE 끼리만이 아니라 UPDATE, DELETE 가 추가된 뒤에도 같은 리소스가 두 번 나오면 거부한다.
- 선례: Supabase 구현은 `resourceType:resourceId` 쌍의 중복을 세어 `Push operation contains a duplicate resource` 로 거부했다 (`../LiftCode/supabase/migrations/20260929000200_create_sync_api.sql` 938-943행).

## 3. 사용자별로 push 를 직렬화한다

- [ ] push 트랜잭션을 시작할 때 사용자 단위 락을 잡는다.

현재 같은 사용자의 push 두 개가 동시에 들어오면 둘 다 재시도 조회와 충돌 조회를 통과한 뒤 쓰기를 시작한다. 쓰기 단계에서 하나가 `programs` 의 기본 키나 `sync_change_log(user_id, operation_id)` 유니크 제약에 걸려 500 으로 실패한다.

- 같은 operation 을 재전송한 경우: 처음 응답(APPLIED)을 돌려줘야 하는데 500 이 된다.
- 서로 다른 operation 이 같은 리소스를 만든 경우: CONFLICT 여야 하는데 500 이 된다.

락을 잡으면 뒤의 push 는 앞의 push 가 커밋될 때까지 기다린 뒤 재시도 조회와 충돌 조회를 하므로 위 두 경우 모두 정상 응답이 된다.

락 방식 후보:

- `users` 행 비관적 락 (`@Lock(PESSIMISTIC_WRITE)`, `SELECT ... FOR UPDATE`): push 가 이미 user 를 조회하므로 쿼리가 늘지 않는다. 다만 이 사용자를 참조하는 다른 트랜잭션의 FK 검사와 users 행 수정도 커밋까지 기다린다.
- PostgreSQL advisory lock (`pg_advisory_xact_lock`): 행을 잠그지 않으므로 다른 트랜잭션에 영향이 없다. native query 가 필요하다. Supabase 구현이 이 방식이다 (`sync_lock_account`, 같은 파일 1-22행, push 870행).

pull 을 구현할 때도 확인한다. `sequence` 는 IDENTITY 라 발급 순서와 커밋 순서가 다를 수 있다. 같은 사용자의 push 가 동시에 커밋되면 pull 이 늦게 커밋된 작은 sequence 를 건너뛰고 커서를 옮길 수 있다. push 를 사용자별로 직렬화하면 이 문제도 막힌다. Supabase 는 pull 에서도 같은 락을 공유 모드로 잡았다 (1092행). pull 을 구현할 때 그 이유를 확인한다.

검증: 두 스레드가 동시에 push 하는 테스트를 두 경우로 추가한다. 하나는 같은 요청을 보내고, 다른 하나는 같은 리소스를 만드는 서로 다른 요청을 보낸다. 테스트 클래스의 `@Transactional` 을 빼야 두 트랜잭션이 실제로 경쟁한다.
