package com.elms.elms_backend.mapper.leave;

import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyResponseDTO;
import com.elms.elms_backend.entity.LeavePolicyEntity;
import org.springframework.stereotype.Component;

@Component
public class LeavePolicyMapper {
    public CreateLeavePolicyResponseDTO mapToResponse(
            LeavePolicyEntity leavePolicy
    ) {
        String leaveTypeName =
                leavePolicy.getLeaveType() != null
                        ? leavePolicy.getLeaveType().getName()
                        : null;
//        List<LeaveActionEnum> allowedActions = new ArrayList<LeaveActionEnum>();
        return new CreateLeavePolicyResponseDTO(
                leavePolicy.getId(),
                leaveTypeName,
                leavePolicy.getYear(),
                leavePolicy.getAllocatedLeave(),
                leavePolicy.getNoticePeriodDays()
        );
    }
}
