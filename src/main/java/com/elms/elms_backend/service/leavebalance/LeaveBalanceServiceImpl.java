package com.elms.elms_backend.service.leavebalance;

import com.elms.elms_backend.dto.leave_analytics.ManagerLeaveAnalyticsProjectionDTO;
import com.elms.elms_backend.dto.leavepolicy.LeaveBalanceProjectionDTO;
import com.elms.elms_backend.entity.LeaveBalanceEntity;
import com.elms.elms_backend.entity.LeavePolicyEntity;
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

    @PreAuthorize("hasRole('MANAGER')")
    @Override
    public List<LeaveBalanceProjectionDTO> getTeamLeaveBalanceProjections() {
        Long managerId = userService.getAuthenticatedUser().getId();
        Integer year = Year.now().getValue();
        return leaveBalanceRepo.getTeamLeaveBalanceProjection(
                managerId,
                year
        );
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Override
    public ManagerLeaveAnalyticsProjectionDTO getManagerLeaveAnalyticsProjection() {
        Long managerId = userService.getAuthenticatedUser().getId();
        Integer year = Year.now().getValue();

        List<LeaveBalanceProjectionDTO> teamLeaveBalances = leaveBalanceRepo.getTeamLeaveBalanceProjection(
                managerId,
                year
        );

        int totalAllocatedLeave = teamLeaveBalances.stream()
                .mapToInt(LeaveBalanceProjectionDTO::getAllocatedLeave)
                .sum();

        int totalRemainingLeave = teamLeaveBalances.stream()
                .mapToInt(LeaveBalanceProjectionDTO::getRemainingLeave)
                .sum();


        // build and return your DTO here
    }


    @Override
    public LeaveBalanceEntity findLeaveBalanceOrThrow(
            UserEntity employee,
            LeavePolicyEntity leavePolicy
    ) {

        LeaveBalanceEntity leaveBalance =
                leaveBalanceRepo.findByEmployeeAndLeavePolicy(
                        employee,
                        leavePolicy
                );

        if (leaveBalance == null) {

            throw new IllegalStateException(
                    "Leave balance not configured."
            );
        }

        return leaveBalance;
    }


}
