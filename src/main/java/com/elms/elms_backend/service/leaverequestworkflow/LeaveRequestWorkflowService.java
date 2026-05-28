package com.elms.elms_backend.service.leaverequestworkflow;

import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.enums.LeaveRequestActionEnum;

import java.util.List;

public interface LeaveRequestWorkflowService {

    List<LeaveRequestActionEnum> allowedLeaveActions(
            LeaveRequestEntity leaveRequest
    );

    void validateTransition(
            LeaveRequestEntity leaveRequest,
            LeaveRequestActionEnum action
    );
}
