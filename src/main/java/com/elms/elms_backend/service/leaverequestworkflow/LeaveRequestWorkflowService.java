package com.elms.elms_backend.service.leaverequestworkflow;

import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.UserEntity;
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

    void assertEmployeeCanSubmitLeave(UserEntity employee);

    void assertManagerCanPerformAction(LeaveRequestEntity leaveRequest);
}
