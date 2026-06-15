package com.elms.elms_backend.service.leavetype;

import com.elms.elms_backend.dto.leavetype.*;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface LeaveTypeService {
    LeaveTypeEntity resolveOptionalLeaveType(String leaveTypeName);
    LeaveTypeEntity resolveLeaveType(String leaveTypeName);
    CreateLeaveTypeResponseDTO createLeaveType(CreateLeaveTypeDTO createLeaveTypeDTO);

    List<String> getActiveLeaveTypes();

    @PreAuthorize("hasRole('ADMIN')")
    List<LeaveTypeProjectionDTO> getLeaveTypes();

    LeaveTypeProjectionDTO updateStatus(Long id , UpdateLeaveTypeStatusDTO dto);

    LeaveTypeProjectionDTO updateName(Long id, RenameLeaveTypeDTO dto);
}
