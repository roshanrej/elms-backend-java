package com.elms.elms_backend.dto.leavepolicy;

import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;

public record LeavePolicyProjectionDTO(Long leaveTypeId,
                                       String leaveTypeName,
                                       LeaveRequestStatusEnum status,
                                       Integer allocatedLeave,
                                       Integer year) {
}
