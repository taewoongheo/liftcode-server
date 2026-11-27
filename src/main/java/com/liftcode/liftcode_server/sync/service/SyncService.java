package com.liftcode.liftcode_server.sync.service;

import com.liftcode.liftcode_server.program.domain.Program;
import com.liftcode.liftcode_server.program.repository.ProgramRepository;
import com.liftcode.liftcode_server.sync.domain.SyncChangeLog;
import com.liftcode.liftcode_server.sync.domain.change.SyncChange;
import com.liftcode.liftcode_server.sync.dto.*;
import com.liftcode.liftcode_server.sync.repository.SyncChangeLogRepository;
import com.liftcode.liftcode_server.user.domain.User;
import com.liftcode.liftcode_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.liftcode.liftcode_server.sync.domain.change.SyncChangeKind.CREATE;
import static com.liftcode.liftcode_server.sync.domain.change.SyncResourceType.PROGRAM;
import static com.liftcode.liftcode_server.sync.dto.PushStatus.APPLIED;
import static com.liftcode.liftcode_server.sync.dto.PushStatus.CONFLICT;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SyncService {

    private final SyncChangeLogRepository syncChangeLogRepository;
    private final UserRepository userRepository;
    private final ProgramRepository programRepository;
    private final SyncValuesReader valuesReader;

    @Transactional
    public PushResponse push(UUID userId, PushRequest request) {

        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalStateException("존재하지 않는 사용자입니다."));

        List<PushResult> results = new ArrayList<>();

        for (PushOperation operation : request.operations()) {

            // 클라이언트가 응답을 받지 못하고 재시도한 경우, 해당 operation 의 응답 결과를 재반환
            Optional<SyncChangeLog> appliedLog =
                    syncChangeLogRepository.findByUserAndOperationId(user, operation.operationId());
            if (appliedLog.isPresent()) {
                results.add(appliedResult(appliedLog.get()));
                continue;
            }

            List<ProgramCreation> creations = readCreations(operation);

            List<ResourceVersion> conflicts = findConflicts(user, creations);
            if (!conflicts.isEmpty()) {
                results.add(new PushResult(operation.operationId(), CONFLICT, null, conflicts));
                break;
            }
            results.add(apply(user, request.deviceId(), operation, creations));
        }

        return new PushResponse(results);
    }

    /**
     * json values 검증
     */
    private List<ProgramCreation> readCreations(PushOperation operation) {
        List<ProgramCreation> creations = new ArrayList<>();

        for (PushChange change : operation.changes()) {
            if (change.resourceType() == PROGRAM && change.changeKind() == CREATE) {
                if (change.baseVersion() != null) {
                    throw new IllegalArgumentException("잘못된 요청입니다");
                }
                ProgramValues values = valuesReader.read(change.values(), ProgramValues.class);
                // 이렇게 null 을 직접 검사하면 필드가 추가될 때마다 검사해야되는데, valuesReader 에서 던지는 예외를 잡아서 처리하는게 나을 듯
                if (values.name() == null) {
                    throw new IllegalArgumentException("이름을 입력해주세요");
                }
                creations.add(new ProgramCreation(change, values));
            }
        }

        return creations;
    }

    private List<ResourceVersion> findConflicts(User user, List<ProgramCreation> creations) {
        List<UUID> createdProgramIds = creations.stream()
                .map(creation -> creation.change().resourceId())
                .toList();

        Map<UUID, Program> existingPrograms = programRepository.findAllById(createdProgramIds).stream()
                .collect(Collectors.toMap(Program::getId, Function.identity()));

        List<ResourceVersion> conflicts = new ArrayList<>();

        for (UUID programId : createdProgramIds) {
            Program existing = existingPrograms.get(programId);
            if (existing == null) {
                continue;
            }
            if (!existing.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("다른 사용자의 리소스입니다.");
            }
            conflicts.add(new ResourceVersion(PROGRAM, existing.getId(), existing.getVersion()));
        }

        return conflicts;
    }

    private PushResult apply(User user, UUID deviceId, PushOperation operation, List<ProgramCreation> creations) {
        List<SyncChange> syncChanges = new ArrayList<>();

        for (ProgramCreation creation : creations) {
            PushChange change = creation.change();
            ProgramValues values = creation.values();

            Program program = programRepository.save(
                    new Program(
                            change.resourceId(),
                            user,
                            values.name(),
                            values.progressStartedAt()));

            syncChanges.add(new SyncChange(
                    change.resourceType(),
                    change.resourceId(),
                    change.changeKind(),
                    program.getVersion(),
                    change.values()
            ));
        }

        SyncChangeLog changeLog = syncChangeLogRepository.save(new SyncChangeLog(
                user,
                operation.operationId(),
                deviceId,
                operation.occurredAt(),
                syncChanges));

        return appliedResult(changeLog);
    }

    private static PushResult appliedResult(SyncChangeLog changeLog) {
        List<ResourceVersion> resources = changeLog.getChanges().stream()
                .map(change -> new ResourceVersion(change.resourceType(), change.resourceId(), change.version()))
                .toList();

        return new PushResult(changeLog.getOperationId(), APPLIED, changeLog.getSequence(), resources);
    }

    public record ProgramValues(String name, Instant progressStartedAt) {}

    private record ProgramCreation(PushChange change, ProgramValues values) {}

}
