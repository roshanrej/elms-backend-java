package com.elms.elms_backend.service.leaveauditlog;

import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.enums.LeaveRequestActionEnum;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;

import java.time.LocalDateTime;

public interface LeaveAuditLogService {


        /**
         * Records an action taken on a leave request.
         *
         * @param leaveRequest   the leave request being acted upon
         * @param action         the action performed
         * @param previousStatus the status before the action (null for creation actions)
         */
        void recordLeaveAction(
                LeaveRequestEntity leaveRequest,
                LeaveRequestActionEnum action,
                LeaveRequestStatusEnum previousStatus
        );
    }

