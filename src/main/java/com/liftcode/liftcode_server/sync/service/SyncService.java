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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.liftcode.liftcode_server.sync.domain.change.SyncChangeKind.CREATE;
import static com.liftcode.liftcode_server.sync.domain.change.SyncResourceType.PROGRAM;
import static com.liftcode.liftcode_server.sync.dto.PushStatus.APPLIED;

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
            List<ResourceVersion> resources = new ArrayList<>();
            List<SyncChange> syncChanges = new ArrayList<>();

            for (PushChange change : operation.changes()) {
                if (change.resourceType() == PROGRAM) {
                    ProgramValues programValues = valuesReader.read(change.values(), ProgramValues.class);

                    if (change.changeKind() == CREATE) {
                        if (programValues.name == null) {
                            throw new IllegalStateException("이름을 입력해주세요");
                        }

                        Program program = programRepository.save(
                                new Program(change.resourceId(),
                                        user,
                                        programValues.name,
                                        programValues.progressStartedAt));
                        syncChanges.add(new SyncChange(
                                change.resourceType(),
                                change.resourceId(),
                                change.changeKind(),
                                /**
                                 * TODO: 로그 반영 후 버전이 들어가도록 수정
                                 */
                                change.baseVersion(),
                                change.values()
                        ));

                        resources.add(new ResourceVersion(PROGRAM, program.getId(), program.getVersion()));
                    }
                }
            }

            SyncChangeLog changeLog = syncChangeLogRepository.save(new SyncChangeLog(
                    user,
                    operation.operationId(),
                    request.deviceId(),
                    operation.occurredAt(),
                    syncChanges));

            results.add(new PushResult(
                    operation.operationId(),
                    APPLIED,
                    changeLog.getSequence(),
                    resources
            ));
        }

        return new PushResponse(results);
    }

    public record ProgramValues(String name, Instant progressStartedAt) {}

}
