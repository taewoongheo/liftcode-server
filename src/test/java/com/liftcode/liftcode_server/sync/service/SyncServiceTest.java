package com.liftcode.liftcode_server.sync.service;

import com.liftcode.liftcode_server.program.domain.Program;
import com.liftcode.liftcode_server.program.repository.ProgramRepository;
import com.liftcode.liftcode_server.sync.domain.SyncChangeLog;
import com.liftcode.liftcode_server.sync.domain.change.SyncChange;
import com.liftcode.liftcode_server.sync.dto.PushChange;
import com.liftcode.liftcode_server.sync.dto.PushOperation;
import com.liftcode.liftcode_server.sync.dto.PushRequest;
import com.liftcode.liftcode_server.sync.dto.PushResponse;
import com.liftcode.liftcode_server.sync.dto.PushResult;
import com.liftcode.liftcode_server.sync.dto.ResourceVersion;
import com.liftcode.liftcode_server.sync.repository.SyncChangeLogRepository;
import com.liftcode.liftcode_server.user.domain.User;
import com.liftcode.liftcode_server.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.liftcode.liftcode_server.sync.domain.change.SyncChangeKind.CREATE;
import static com.liftcode.liftcode_server.sync.domain.change.SyncResourceType.PROGRAM;
import static com.liftcode.liftcode_server.sync.dto.PushStatus.APPLIED;
import static com.liftcode.liftcode_server.sync.dto.PushStatus.CONFLICT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SyncServiceTest {

    @Autowired
    private SyncService syncService;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private SyncChangeLogRepository syncChangeLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("Program 생성 시 Program 과 ChangeLog 가 저장된다")
    void syncCreateProgram() {
        User user = userRepository.save(new User(UUID.randomUUID()));
        PushRequest request = pushRequest(List.of(
                createProgram(Map.of("name", "ProgramA", "progressStartedAt", "2026-11-26T09:00:00Z")),
                createProgram(Map.of("name", "ProgramB", "progressStartedAt", "2026-11-26T09:00:00Z"))));

        syncService.push(user.getId(), request);

        assertThat(programRepository.findAll().size()).isEqualTo(2);
        assertThat(syncChangeLogRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("생성이 반영되면 APPLIED 결과에 sequence 와 각 리소스의 첫 버전(1)을 담아 응답한다")
    void returnAppliedResultWithFirstVersion() {
        User user = newUser();
        PushChange programA = createProgram(programValues("ProgramA"));
        PushChange programB = createProgram(programValues("ProgramB"));
        PushRequest request = pushRequest(List.of(programA, programB));

        PushResponse response = syncService.push(user.getId(), request);

        assertThat(response.results()).hasSize(1);
        PushResult result = response.results().get(0);
        assertThat(result.operationId()).isEqualTo(request.operations().get(0).operationId());
        assertThat(result.status()).isEqualTo(APPLIED);
        assertThat(result.sequence()).isNotNull();
        assertThat(result.resources()).containsExactly(
                new ResourceVersion(PROGRAM, programA.resourceId(), 1),
                new ResourceVersion(PROGRAM, programB.resourceId(), 1));
    }

    @Test
    @DisplayName("ChangeLog 에는 반영 후 버전과 반영한 values 가 저장된다")
    void recordAppliedChangesInChangeLog() {
        User user = newUser();
        PushChange programA = createProgram(programValues("ProgramA"));
        PushChange programB = createProgram(programValues("ProgramB"));

        PushResponse response = syncService.push(user.getId(), pushRequest(List.of(programA, programB)));
        flushAndClear();

        SyncChangeLog changeLog = syncChangeLogRepository.findById(response.results().get(0).sequence()).orElseThrow();
        assertThat(changeLog.getChanges()).containsExactly(
                new SyncChange(PROGRAM, programA.resourceId(), CREATE, 1, programA.values()),
                new SyncChange(PROGRAM, programB.resourceId(), CREATE, 1, programB.values()));
    }

    @Test
    @DisplayName("같은 operation 을 다시 보내면 다시 반영하지 않고 처음과 같은 결과를 돌려준다")
    void applyRetriedOperationOnce() {
        User user = newUser();
        PushRequest request = pushRequest(List.of(createProgram(programValues("ProgramA"))));

        PushResponse first = syncService.push(user.getId(), request);
        flushAndClear();
        PushResponse retry = syncService.push(user.getId(), request);
        flushAndClear();

        assertThat(retry.results()).isEqualTo(first.results());
        assertThat(changeLogCount(user)).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 있는 내 리소스 ID 로 생성하면 CONFLICT 로 응답하고 기존 행은 바뀌지 않는다")
    void conflictWhenCreatingExistingResource() {
        User user = newUser();
        UUID programId = UUID.randomUUID();
        PushResponse created = syncService.push(user.getId(),
                pushRequest(List.of(createProgram(programId, programValues("Original")))));
        flushAndClear();

        PushResponse response = syncService.push(user.getId(),
                pushRequest(List.of(createProgram(programId, programValues("Duplicate")))));
        flushAndClear();

        PushResult result = response.results().get(0);
        assertThat(result.status()).isEqualTo(CONFLICT);
        assertThat(result.sequence()).isNull();
        assertThat(result.resources()).containsExactlyElementsOf(created.results().get(0).resources());
        assertThat(programRepository.findById(programId).orElseThrow().getName()).isEqualTo("Original");
        assertThat(changeLogCount(user)).isEqualTo(1);
    }

    @Test
    @DisplayName("다른 사용자의 리소스 ID 로 생성하면 요청을 거부하고 그 행은 바뀌지 않는다")
    void rejectCreatingResourceOwnedByAnotherUser() {
        User owner = newUser();
        User other = newUser();
        UUID programId = UUID.randomUUID();
        syncService.push(owner.getId(), pushRequest(List.of(createProgram(programId, programValues("Owner's")))));
        flushAndClear();

        PushRequest request = pushRequest(List.of(createProgram(programId, programValues("Taken"))));
        assertThatThrownBy(() -> syncService.push(other.getId(), request))
                .isInstanceOf(IllegalArgumentException.class);

        Program program = programRepository.findById(programId).orElseThrow();
        assertThat(program.getUser().getId()).isEqualTo(owner.getId());
        assertThat(program.getName()).isEqualTo("Owner's");
    }

    // ---- 입력 검증 ----

    @Test
    @DisplayName("Program values 에 명세에 없는 키가 있으면 요청을 거부한다")
    void rejectUnknownProgramValueKey() {
        User user = userRepository.save(new User(UUID.randomUUID()));
        PushRequest request = pushRequest(List.of(
                createProgram(Map.of(
                        "name", "ProgramA",
                        "progressStartedAt", "2026-11-26T09:00:00Z",
                        "nmae", "typo"))));

        assertThatThrownBy(() -> syncService.push(user.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nmae");
    }

    @Test
    @DisplayName("Program values 의 타입이 틀리면 요청을 거부한다")
    void rejectInvalidProgramValueType() {
        User user = newUser();
        PushRequest request = pushRequest(List.of(
                createProgram(Map.of("name", "ProgramA", "progressStartedAt", "not-a-date"))));

        assertThatThrownBy(() -> syncService.push(user.getId(), request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Program 생성에 name 이 없으면 요청을 거부한다")
    void rejectCreateWithoutName() {
        User user = newUser();
        PushRequest request = pushRequest(List.of(
                createProgram(Map.of("progressStartedAt", "2026-11-26T09:00:00Z"))));

        assertThatThrownBy(() -> syncService.push(user.getId(), request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("생성 요청의 baseVersion 이 0 이 아니면 요청을 거부한다")
    void rejectCreateWithNonZeroBaseVersion() {
        User user = newUser();
        PushChange change = new PushChange(PROGRAM, UUID.randomUUID(), CREATE, 3, programValues("ProgramA"));

        assertThatThrownBy(() -> syncService.push(user.getId(), pushRequest(List.of(change))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private User newUser() {
        return userRepository.save(new User(UUID.randomUUID()));
    }

    private static Map<String, Object> programValues(String name) {
        return Map.of("name", name, "progressStartedAt", "2026-11-26T09:00:00Z");
    }

    private static PushChange createProgram(Map<String, Object> values) {
        return createProgram(UUID.randomUUID(), values);
    }

    private static PushChange createProgram(UUID programId, Map<String, Object> values) {
        return new PushChange(PROGRAM, programId, CREATE, 0, values);
    }

    private static PushRequest pushRequest(List<PushChange> changes) {
        return new PushRequest(UUID.randomUUID(), List.of(
                new PushOperation(UUID.randomUUID(), Instant.now(), changes)));
    }

    // 다음 push 가 별도 요청처럼 DB 에서 다시 읽도록 영속성 컨텍스트를 비운다.
    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private long changeLogCount(User user) {
        return em.createQuery("select count(l) from SyncChangeLog l where l.user.id = :userId", Long.class)
                .setParameter("userId", user.getId())
                .getSingleResult();
    }
}
