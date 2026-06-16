package com.elms.elms_backend.service.leavebalance;

import com.elms.elms_backend.dto.leavepolicy.LeaveBalanceProjectionDTO;
import com.elms.elms_backend.entity.LeaveBalanceEntity;
import com.elms.elms_backend.entity.LeavePolicyEntity;
import com.elms.elms_backend.entity.UserEntity;

import java.util.List;


public interface LeaveBalanceService {
    List<LeaveBalanceProjectionDTO> getEmployeeLeaveBalanceProjections();

    LeaveBalanceEntity findLeaveBalanceOrThrow(
            UserEntity employee,
            LeavePolicyEntity leavePolicy
    );
}
