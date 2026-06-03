package com.elms.elms_backend.service.leavepolicy;


import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyDTO;
import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyResponseDTO;
import com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO;
import com.elms.elms_backend.entity.LeaveBalanceEntity;
import com.elms.elms_backend.entity.LeavePolicyEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.mapper.leave.LeavePolicyMapper;
import com.elms.elms_backend.repository.leave.LeaveBalanceRepository;
import com.elms.elms_backend.repository.leave.LeavePolicyRepository;
import com.elms.elms_backend.service.leavetype.LeaveTypeService;
import com.elms.elms_backend.service.user.UserService;
import org.hibernate.grammars.hql.HqlParser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
@Service
public class LeavePolicyServiceImpl implements LeavePolicyService {
    private final LeavePolicyRepository leavePolicyRepo;
    private final LeaveTypeService leaveTypeService;
    private final LeavePolicyMapper leavePolicyMapper;
    private final UserService userService;
    private final LeaveBalanceRepository leaveBalanceRepo;

    public LeavePolicyServiceImpl(LeavePolicyRepository leavePolicyRepo, LeaveTypeService leaveTypeService, LeavePolicyMapper leavePolicyMapper, UserService userService, LeaveBalanceRepository leaveBalanceRepo) {
        this.leavePolicyRepo = leavePolicyRepo;

        this.leaveTypeService = leaveTypeService;
        this.leavePolicyMapper = leavePolicyMapper;
        this.userService = userService;
        this.leaveBalanceRepo = leaveBalanceRepo;
    }


    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    @Override
    public List<LeavePolicyProjectionDTO> getCurrentActiveLeavePolicies() {
        Integer year = Year.now().getValue();
        return leavePolicyRepo.findPoliciesByYearAndStatus(year, LeaveTypeStatusEnum.ACTIVE);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<LeavePolicyProjectionDTO> getLeavePolicies(Integer year) {
        return leavePolicyRepo.findPoliciesByYear(year);
    }

    /**
     * @param createLeavePolicyDTO
     * @return leave policy projection on specific leave Type, year
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @Transactional
    public CreateLeavePolicyResponseDTO createLeavePolicy(CreateLeavePolicyDTO createLeavePolicyDTO) {
        Integer year = createLeavePolicyDTO.getYear();
        Integer allocatedLeave = createLeavePolicyDTO.getAllocatedLeave();
        LeaveTypeEntity leaveTypeEntity = leaveTypeService.resolveLeaveType(createLeavePolicyDTO.getLeaveType()); // check for valid leave Type
        if (leavePolicyRepo.existsByLeaveTypeAndYear(leaveTypeEntity, year)) {
            throw new IllegalStateException("Leave policy already exists");
        }
        if(allocatedLeave <= 0){
            throw new IllegalArgumentException("Allocated leave must be greater than zero");
        }
        LeavePolicyEntity leavePolicyEntity = LeavePolicyEntity.builder()
                .leaveType( leaveTypeEntity)
                .year(year)
                .allocatedLeave(allocatedLeave)
                .build();
        LeavePolicyEntity savedLeavePolicyEntity = leavePolicyRepo.save(leavePolicyEntity);

        List<UserEntity> employees = userService.findByRole(RoleEnum.EMPLOYEE);
        List<LeaveBalanceEntity> balances = employees.stream()
                .map(employee -> LeaveBalanceEntity.builder()
                        .employee(employee)
                        .leavePolicy(savedLeavePolicyEntity)
                        .consumedLeave(0)
                        .remainingLeave(allocatedLeave)
                        .updatedAt(LocalDateTime.now())
                        .build())
                .toList();

        leaveBalanceRepo.saveAll(balances);
       return leavePolicyMapper.mapToResponse(savedLeavePolicyEntity);
    }

    @Override
    public LeavePolicyEntity findLeavePolicyOrThrow(
            LeaveTypeEntity leaveType,
            Integer year
    ) {

        LeavePolicyEntity policy =
                leavePolicyRepo.findByLeaveTypeAndYear(
                        leaveType,
                        year
                );

        if (policy == null) {

            throw new IllegalStateException(
                    "No leave policy configured for leave type '"
                            + leaveType.getName()
                            + "' in year "
                            + year
                            + "."
            );
        }

        return policy;
    }


    /**
     * Calculates the total number of days between two dates inclusively.
     * * @param startDate start date
     *
     * @param endDate end date
     * @return total days
     */
    @Override
    public Integer calculateLeaveDays(LocalDate startDate, LocalDate endDate) {
        int noOfDays = 0;
        LocalDate current = startDate;
        while(!current.isAfter(endDate)){
            if (isWorkingDay(current)) {
                noOfDays++;
            }
            current = current.plusDays(1);
        }
        return  noOfDays;
    }
    private boolean isWorkingDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        return dayOfWeek != DayOfWeek.SATURDAY
                && dayOfWeek != DayOfWeek.SUNDAY;
    }
}
