package com.elms.elms_backend.service.leavetype;

import com.elms.elms_backend.dto.leavetype.CreateLeaveTypeDTO;
import com.elms.elms_backend.dto.leavetype.CreateLeaveTypeResponseDTO;
import com.elms.elms_backend.entity.LeaveTypeEntity;

public interface LeaveTypeService {
    LeaveTypeEntity resolveOptionalLeaveType(String leaveTypeName);
    LeaveTypeEntity resolveLeaveType(String leaveTypeName);
    CreateLeaveTypeResponseDTO createLeaveType(CreateLeaveTypeDTO createLeaveTypeDTO);

}
