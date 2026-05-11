package com.elms.elms_backend.service.leave;

import com.elms.elms_backend.dto.leave.LeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveResponseDTO;
import com.elms.elms_backend.entity.enums.LeaveActionEnum;

public  interface LeaveRequestService {
    LeaveResponseDTO createLeave(LeaveRequestDTO request);
}