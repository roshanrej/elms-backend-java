package com.elms.elms_backend.service.leavebalance;

import com.elms.elms_backend.dto.leavepolicy.LeaveBalanceProjectionDTO;
import com.elms.elms_backend.entity.LeaveBalanceEntity;
import com.elms.elms_backend.entity.LeavePolicyEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
import com.elms.elms_backend.repository.leave.LeaveBalanceRepository;
import com.elms.elms_backend.repository.leave.LeavePolicyRepository;
import com.elms.elms_backend.entity.RoleEntity;
import com.elms.elms_backend.repository.user.RoleRepository;
import com.elms.elms_backend.repository.user.UserRepository;
import com.elms.elms_backend.service.user.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class LeaveBalanceServiceImpl implements LeaveBalanceService {
    private static final Set<RoleEnum> ELEVATED_ROLES = EnumSet.of(
            RoleEnum.MANAGER,
            RoleEnum.ADMIN
    );

    private final LeaveBalanceRepository leaveBalanceRepo;
    private final LeavePolicyRepository leavePolicyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;


    public LeaveBalanceServiceImpl(
            LeaveBalanceRepository leaveBalanceRepo,
            LeavePolicyRepository leavePolicyRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserService userService
    ) {
        this.leaveBalanceRepo = leaveBalanceRepo;
        this.leavePolicyRepository = leavePolicyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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

    @Override
    @Transactional
    public void provisionEmployeeBalances(UserEntity employee) {
        if (employee.getRole().getName() != RoleEnum.EMPLOYEE) {
            return;
        }

        List<LeavePolicyEntity> policies = leavePolicyRepository.findAll();
        for (LeavePolicyEntity policy : policies) {
            LeaveBalanceEntity existing = leaveBalanceRepo.findByEmployeeAndLeavePolicy(employee, policy);
            if (existing == null) {
                leaveBalanceRepo.save(
                        LeaveBalanceEntity.builder()
                                .employee(employee)
                                .leavePolicy(policy)
                                .consumedLeave(0)
                                .remainingLeave(policy.getAllocatedLeave())
                                .updatedAt(LocalDateTime.now())
                                .build()
                );
            }
        }
    }

    @Override
    @Transactional
    public void createBalancesForPolicy(LeavePolicyEntity leavePolicy) {
        RoleEntity employeeRole = roleRepository.findByName(RoleEnum.EMPLOYEE)
                .orElseThrow(() -> new IllegalStateException("Employee role not configured"));
        List<UserEntity> employees = userRepository.findByRoleAndStatus(
                employeeRole,
                UserStatusEnum.ACTIVE
        );

        List<LeaveBalanceEntity> balances = employees.stream()
                .filter(employee -> leaveBalanceRepo.findByEmployeeAndLeavePolicy(employee, leavePolicy) == null)
                .map(employee -> LeaveBalanceEntity.builder()
                        .employee(employee)
                        .leavePolicy(leavePolicy)
                        .consumedLeave(0)
                        .remainingLeave(leavePolicy.getAllocatedLeave())
                        .updatedAt(LocalDateTime.now())
                        .build())
                .toList();
        leaveBalanceRepo.saveAll(balances);
    }

    @Override
    @Transactional
    public void handleRoleChangeBalances(UserEntity user, RoleEnum oldRole, RoleEnum newRole) {
        if (oldRole == RoleEnum.EMPLOYEE && ELEVATED_ROLES.contains(newRole)) {
            updateBalancesTimestamp(user);
        } else if (ELEVATED_ROLES.contains(oldRole) && newRole == RoleEnum.EMPLOYEE) {
            updateBalancesTimestamp(user);
            provisionEmployeeBalances(user);
        } else if (ELEVATED_ROLES.contains(oldRole) && ELEVATED_ROLES.contains(newRole)) {
            updateBalancesTimestamp(user);
        }
    }

    private void updateBalancesTimestamp(UserEntity user) {
        leaveBalanceRepo.findByEmployee(user).forEach(balance -> {
            balance.setUpdatedAt(LocalDateTime.now());
            leaveBalanceRepo.save(balance);
        });
    }
}
