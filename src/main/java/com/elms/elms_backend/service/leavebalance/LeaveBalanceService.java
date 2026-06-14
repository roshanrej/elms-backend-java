package com.elms.elms_backend.service.leavebalance;

import com.elms.elms_backend.dto.leave_analytics.ManagerLeaveAnalyticsProjectionDTO;
import com.elms.elms_backend.dto.leavepolicy.LeaveBalanceProjectionDTO;
import com.elms.elms_backend.entity.LeaveBalanceEntity;
import com.elms.elms_backend.entity.LeavePolicyEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.UserEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;


public interface LeaveBalanceService {
    public List<LeaveBalanceProjectionDTO> getEmployeeLeaveBalanceProjections();

    @PreAuthorize("hasRole('EMPLOYEE')")
    List<LeaveBalanceProjectionDTO> getTeamLeaveBalanceProjections();

    @PreAuthorize("hasRole('MANAGER')")
    ManagerLeaveAnalyticsProjectionDTO getManagerLeaveAnalyticsProjection();

    LeaveBalanceEntity findLeaveBalanceOrThrow(
            UserEntity employee,
            LeavePolicyEntity leavePolicy
    );
}
