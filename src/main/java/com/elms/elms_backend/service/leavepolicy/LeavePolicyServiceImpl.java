package com.elms.elms_backend.service.leavepolicy;


import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyDTO;
import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyResponseDTO;
import com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO;
import com.elms.elms_backend.entity.LeavePolicyEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import com.elms.elms_backend.mapper.leave.LeavePolicyMapper;
import com.elms.elms_backend.repository.leave.LeavePolicyRepository;
import com.elms.elms_backend.service.leavebalance.LeaveBalanceService;
import com.elms.elms_backend.service.leavetype.LeaveTypeService;
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
    private final LeaveBalanceService leaveBalanceService;

    public LeavePolicyServiceImpl(
            LeavePolicyRepository leavePolicyRepo,
            LeaveTypeService leaveTypeService,
            LeavePolicyMapper leavePolicyMapper,
            LeaveBalanceService leaveBalanceService
    ) {
        this.leavePolicyRepo = leavePolicyRepo;
        this.leaveTypeService = leaveTypeService;
        this.leavePolicyMapper = leavePolicyMapper;
        this.leaveBalanceService = leaveBalanceService;
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
     * @param createLeavePolicyDTO incoming create leave policy dto
     * @return leave policy projection on specific leave Type, year
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @Transactional
    public CreateLeavePolicyResponseDTO createLeavePolicy(CreateLeavePolicyDTO createLeavePolicyDTO) {
        Integer year = createLeavePolicyDTO.getYear();
        Integer allocatedLeave = createLeavePolicyDTO.getAllocatedLeave();
        LeaveTypeEntity leaveTypeEntity = leaveTypeService.resolveLeaveType(createLeavePolicyDTO.getLeaveType());
        Integer noticePeriodDays = createLeavePolicyDTO.getNoticePeriodDays();
        // check for valid leave Type
        if (leavePolicyRepo.existsByLeaveTypeAndYear(leaveTypeEntity, year)) {
            throw new IllegalStateException("Leave policy already exists");
        }
        if(allocatedLeave <= 0){
            throw new IllegalArgumentException("Allocated leave must be greater than zero");
        }
        if(noticePeriodDays < 0){
            throw new IllegalArgumentException("Notice period cannot be less than zero");
        }
        LeavePolicyEntity leavePolicyEntity = LeavePolicyEntity.builder()
                .leaveType( leaveTypeEntity)
                .year(year)
                .allocatedLeave(allocatedLeave)
                .noticePeriodDays(noticePeriodDays)
                .build();
        LeavePolicyEntity savedLeavePolicyEntity = leavePolicyRepo.save(leavePolicyEntity);
        leaveBalanceService.createBalancesForPolicy(savedLeavePolicyEntity);
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
     * Calculates the total number of days between two dates based on leave policy.
     * * @param startDate start date
     *
     * @param endDate end date
     * @return total days
     */
    @Override
    public Integer calculateLeaveDays(LocalDate startDate, LocalDate endDate) {
        if(startDate == null || endDate == null){
            return null;
        }
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

    /**
     * Validates that the leave request does not span multiple years.
     *
     * @throws IllegalArgumentException if startDate and endDate are not in the same year.
     */
    @Override
    public void validateLeaveDates(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate.getYear() != endDate.getYear()) {
            throw new IllegalArgumentException(
                    "Leave requests cannot span multiple years."
            );
        }
    }
    /**
     * Validates that the requested leave date meets the minimum notice period.
     * * @param startDate the requested start date
     *
     * @throws IllegalStateException if the notice period is violated
     */

    @Override
    public void validateNoticePeriod(LeavePolicyEntity leavePolicy, LocalDate startDate) {
        LocalDate today = LocalDate.now();

        if (startDate.isBefore(today)) {
            throw new IllegalStateException("Leave start date cannot be in the past.");
        }

        long noticeDays = ChronoUnit.DAYS.between(today, startDate);
        Integer noticePeriodDays = leavePolicy.getNoticePeriodDays();
        if(noticePeriodDays != 0){
            if (noticeDays <   noticePeriodDays){
            throw new IllegalStateException("Leave requests must be submitted at least " + noticePeriodDays + " days in advance.");
        }
        }
    }
    private boolean isWorkingDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY
                && dayOfWeek != DayOfWeek.SUNDAY;
    }
}
