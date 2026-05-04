package com.elms.elms_backend.service.leave;

import com.elms.elms_backend.dto.leave.LeaveRequestDTO;
import com.elms.elms_backend.entity.LeaveRequest;




public  interface LeaveRequestService {

    LeaveRequest createLeave(LeaveRequestDTO request);
}