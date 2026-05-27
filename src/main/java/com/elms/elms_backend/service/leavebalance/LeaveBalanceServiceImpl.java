package com.elms.elms_backend.service.leavebalance;

import com.elms.elms_backend.dto.leavepolicy.LeaveBalanceProjectionDTO;
import com.elms.elms_backend.entity.LeaveBalanceEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.repository.leave.LeaveBalanceRepository;
import com.elms.elms_backend.service.user.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;

@Service
public class LeaveBalanceServiceImpl implements LeaveBalanceService {
    private final LeaveBalanceRepository leaveBalanceRepo;
    private final UserService userService;


    public LeaveBalanceServiceImpl(LeaveBalanceRepository leaveBalanceRepo, UserService userService) {
        this.leaveBalanceRepo = leaveBalanceRepo;
        this.userService = userService;
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @Override
    public List<LeaveBalanceProjectionDTO> getEmployeeLeaveBalanceProjections() {
        UserEntity employee = userService.getAuthenticatedUser();
        Integer year = Year.now().getValue();
        return leaveBalanceRepo.getLeaveBalanceProjection(
                employee,
                year
        );
    }
}
