package com.elms.elms_backend.service.leavebalance;

import com.elms.elms_backend.entity.LeaveBalanceEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.repository.leave.LeaveBalanceRepository;
import org.springframework.stereotype.Service;

@Service
public class LeaveBalanceServiceImpl implements LeaveBalanceService{
    private final LeaveBalanceRepository leaveBalanceRepo;

    public LeaveBalanceServiceImpl(LeaveBalanceRepository leaveBalanceRepo) {
        this.leaveBalanceRepo = leaveBalanceRepo;
    }

    /**
     * Gets total consumed leaves for leave type
     *
     * @param user authenticated employee
     * @param leaveType leaveType
     * @param year
     * @return persisted number of leave days for employee
     */

    public Integer getTotalConsumedLeavesByEmployee(
            UserEntity user,
            LeaveTypeEntity leaveType,
            Integer year
    ){
//        LeaveBalanceEntity leaveBalance = leaveBalanceRepo.findByUserAndLeavePolicy(user,leaveType);
        return 0;
    }
}
