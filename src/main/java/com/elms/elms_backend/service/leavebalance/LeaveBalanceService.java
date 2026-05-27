package com.elms.elms_backend.service.leavebalance;

import com.elms.elms_backend.dto.leavepolicy.LeaveBalanceProjectionDTO;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.UserEntity;

import java.util.List;


public interface LeaveBalanceService {
    public List<LeaveBalanceProjectionDTO> getEmployeeLeaveBalanceProjections();

}
