package com.elms.elms_backend.dto.leavepolicy;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;

public record LeavePolicyProjectionDTO(Long leaveTypeId,
                                       String leaveTypeName,
                                       LeaveTypeStatusEnum status,
                                       Integer allocatedLeave,
                                       Integer year) {
}
