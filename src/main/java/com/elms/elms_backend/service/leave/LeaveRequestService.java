package com.elms.elms_backend.service.leave;

import com.elms.elms_backend.dto.leave.LeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveResponseDTO;
import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.enums.LeaveActionEnum;

import java.util.List;

public  interface LeaveRequestService {
    LeaveResponseDTO submitNewLeaveRequest(LeaveRequestDTO requestDTO);
    LeaveResponseDTO createLeaveDraft(LeaveRequestDTO requestDTO);
    LeaveResponseDTO submitLeaveRequest(Long id, LeaveRequestDTO leaveRequestDTO);
    LeaveResponseDTO requestLeaveCancel(Long id);
    LeaveResponseDTO cancelLeaveRequest(Long id);
    List<LeaveResponseDTO> getEmployeeLeaves();

}