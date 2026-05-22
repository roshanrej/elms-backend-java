package com.elms.elms_backend.service.leavebalance;

import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.UserEntity;



public interface LeaveBalanceService {
    Integer getTotalConsumedLeavesByEmployee(UserEntity user, LeaveTypeEntity leaveType, Integer Year);

}
