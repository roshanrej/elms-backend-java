package com.elms.elms_backend.service.leavetype;

import com.elms.elms_backend.dto.leavetype.LeaveTypeResponseDTO;
import com.elms.elms_backend.entity.LeaveTypeEntity;

import java.util.List;

public interface LeaveTypeService {
    LeaveTypeEntity resolveOptionalLeaveType(String leaveTypeName);
    LeaveTypeEntity resolveLeaveType(String leaveTypeName);
    List<LeaveTypeResponseDTO> getLeaveTypes();
}
