package com.elms.elms_backend.service.leaveauditlog;

import com.elms.elms_backend.entity.LeaveAuditLogEntity;
import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.LeaveRequestActionEnum;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import com.elms.elms_backend.repository.leave.LeaveAuditLogRepository;
import com.elms.elms_backend.service.leaverequestworkflow.LeaveRequestWorkflowService;
import com.elms.elms_backend.service.user.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class LeaveAuditLogServiceImpl implements LeaveAuditLogService {

    private final UserService userService;
    private final LeaveAuditLogRepository leaveAuditLogRepo;

    public LeaveAuditLogServiceImpl(UserService userService, LeaveAuditLogRepository leaveAuditLogRepo) {
        this.userService = userService;
        this.leaveAuditLogRepo = leaveAuditLogRepo;
    }

    @Override
    public void recordLeaveAction(
            LeaveRequestEntity leaveRequest,
            LeaveRequestActionEnum action,
            LeaveRequestStatusEnum previousStatus
    ) {
        UserEntity actor = userService.getAuthenticatedUser();
        Map<String, Object> metadata = new HashMap<>();

        // previousStatus is null for creation actions (SAVE_DRAFT, SUBMIT_REQUEST on new requests)
        metadata.put(
                "previousStatus",
                previousStatus != null ? previousStatus.name() : null
        );
        metadata.put("newStatus",      leaveRequest.getStatus().name());
        metadata.put("leaveRequestId", leaveRequest.getId());
        metadata.put("employeeId",     leaveRequest.getEmployee().getId());

        LeaveAuditLogEntity leaveAuditLog = LeaveAuditLogEntity.builder()
                .action(action)
                .actor(actor)
                .leaveRequest(leaveRequest)
                .createdAt(LocalDateTime.now())
                .actorRole(actor.getRole())
                .metadata(metadata)
                .build();

        leaveAuditLogRepo.save(leaveAuditLog);
    }
}
