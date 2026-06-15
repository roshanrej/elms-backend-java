package com.elms.elms_backend.service.leavetype;

import com.elms.elms_backend.dto.leavetype.CreateLeaveTypeDTO;
import com.elms.elms_backend.dto.leavetype.CreateLeaveTypeResponseDTO;
import com.elms.elms_backend.dto.leavetype.LeaveTypeProjectionDTO;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface LeaveTypeService {
    LeaveTypeEntity resolveOptionalLeaveType(String leaveTypeName);
    LeaveTypeEntity resolveLeaveType(String leaveTypeName);
    CreateLeaveTypeResponseDTO createLeaveType(CreateLeaveTypeDTO createLeaveTypeDTO);

    @PreAuthorize("hasRole('ADMIN")
    List<String> getActiveLeaveTypes();

    @PreAuthorize("hasRole('ADMIN")
    List<LeaveTypeProjectionDTO> getLeaveTypes();
}
