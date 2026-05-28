package com.elms.elms_backend.service.leaverequest;

import com.elms.elms_backend.dto.leave.CreateLeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveRequestProjectionDTO;
import com.elms.elms_backend.dto.leave.ManagerEmployeeLeaveDTO;
import com.elms.elms_backend.entity.*;
import com.elms.elms_backend.entity.enums.LeaveRequestActionEnum;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import com.elms.elms_backend.mapper.leave.LeaveRequestMapper;
import com.elms.elms_backend.repository.leave.LeaveBalanceRepository;
import com.elms.elms_backend.repository.leave.LeaveRequestRepository;
import com.elms.elms_backend.service.leavebalance.LeaveBalanceService;
import com.elms.elms_backend.service.leavepolicy.LeavePolicyService;
import com.elms.elms_backend.service.leaverequestworkflow.LeaveRequestWorkflowService;
import com.elms.elms_backend.service.leavetype.LeaveTypeService;
import com.elms.elms_backend.service.user.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import java.util.List;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private static final int MINIMUM_NOTICE_DAYS = 14;

    private final LeaveRequestRepository leaveRequestRepo;

    private final UserService userService;

    private final LeaveTypeService leaveTypeService;

    private final LeaveRequestMapper leaveRequestMapper;

private final LeaveRequestWorkflowService leaveRequestWorkflowService;
    private final LeaveBalanceRepository leaveBalanceRepo;

    private final LeaveBalanceService leaveBalanceService;
    private final LeavePolicyService leavePolicyService;


    public LeaveRequestServiceImpl(
            LeaveRequestRepository leaveRequestRepo,
            UserService userService,
            LeaveTypeService leaveTypeService,
            LeaveRequestMapper leaveRequestMapper, LeaveRequestWorkflowService leaveRequestWorkflowService,

            LeaveBalanceRepository leaveBalanceRepo,
            LeaveBalanceService leaveBalanceService, LeavePolicyService leavePolicyService
    ) {

        this.leaveRequestRepo = leaveRequestRepo;
        this.userService = userService;
        this.leaveTypeService = leaveTypeService;
        this.leaveRequestMapper = leaveRequestMapper;
        this.leaveRequestWorkflowService = leaveRequestWorkflowService;
        this.leavePolicyService = leavePolicyService;
        this.leaveBalanceRepo = leaveBalanceRepo;
        this.leaveBalanceService = leaveBalanceService;
    }


    // =========================================================================
    // PUBLIC WORKFLOW ENTRY POINTS
    // =========================================================================


    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public LeaveRequestProjectionDTO createLeaveDraft(
            CreateLeaveRequestDTO createLeaveRequestDto
    ) {

        UserEntity employee =
                userService.getAuthenticatedUser();

        userService.requireAssignedManager(employee);

        LeaveTypeEntity leaveType =
                leaveTypeService.resolveOptionalLeaveType(
                        createLeaveRequestDto.getLeaveType()
                );

        Integer year =
                extractYear(
                        createLeaveRequestDto.getStartDate()
                );

        LeaveRequestEntity savedLeave =
                buildAndSaveLeaveRequest(
                        employee,
                        leaveType,
                        createLeaveRequestDto,
                        LeaveRequestStatusEnum.DRAFT,
                        year,
                        null
                );

        return mapWithActions(savedLeave);
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public LeaveRequestProjectionDTO submitNewLeaveRequest(
            CreateLeaveRequestDTO createLeaveRequestDto
    ) {

        // =====================================================
        // AUTHENTICATION / ORGANIZATIONAL CONTEXT
        // =====================================================

        UserEntity employee =
                userService.getAuthenticatedUser();

        userService.requireAssignedManager(employee);


        // =====================================================
        // TRANSPORT / PAYLOAD VALIDATION
        // =====================================================

        validateSubmissionPayload(createLeaveRequestDto);


        // =====================================================
        // REQUEST CONTEXT EXTRACTION
        // =====================================================

        LocalDate startDate =
                createLeaveRequestDto.getStartDate();

        LocalDate endDate =
                createLeaveRequestDto.getEndDate();

        Integer year =
                extractYear(startDate);


        // =====================================================
        // GLOBAL DOMAIN INVARIANTS
        // =====================================================

        validateNoticePeriod(startDate);


        // =====================================================
        // DOMAIN / POLICY RESOLUTION
        // =====================================================

        LeaveTypeEntity leaveType =
                leaveTypeService.resolveLeaveType(
                        createLeaveRequestDto.getLeaveType()
                );

        LeavePolicyEntity leavePolicy =
                leavePolicyService.findLeavePolicyOrThrow(
                        leaveType,
                        year
                );


        // =====================================================
        // OPERATIONAL CALCULATIONS
        // =====================================================

        Integer noOfDays =
                calculateLeaveDays(
                        startDate,
                        endDate
                );


        // =====================================================
        // BALANCE RESOLUTION / VALIDATION
        // =====================================================

        LeaveBalanceEntity leaveBalance =
                leaveBalanceService.findLeaveBalanceOrThrow(
                        employee,
                        leavePolicy
                );

        validateLeaveBalance(
                leaveBalance,
                noOfDays
        );


        // =====================================================
        // PERSISTENCE
        // =====================================================

        LeaveRequestEntity savedLeave =
                buildAndSaveLeaveRequest(
                        employee,
                        leaveType,
                        createLeaveRequestDto,
                        LeaveRequestStatusEnum.PENDING,
                        year,
                        LocalDateTime.now()
                );


        // =====================================================
        // RESPONSE PROJECTION
        // =====================================================

        return mapWithActions(savedLeave);
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public LeaveRequestProjectionDTO submitLeaveRequest(
            Long id,
            CreateLeaveRequestDTO createLeaveRequestDto
    ) {

        UserEntity employee =
                userService.getAuthenticatedUser();

        userService.requireAssignedManager(employee);

        LeaveRequestEntity leaveRequest =
                findLeaveOrThrow(id);

        leaveRequestWorkflowService.validateTransition(
                leaveRequest,
                LeaveRequestActionEnum.SUBMIT_REQUEST
        );

        validateSubmissionPayload(createLeaveRequestDto);

        LocalDate startDate =
                createLeaveRequestDto.getStartDate();

        LocalDate endDate =
                createLeaveRequestDto.getEndDate();

        Integer year =
                extractYear(startDate);

        validateNoticePeriod(startDate);

        LeaveTypeEntity leaveType =
                leaveTypeService.resolveLeaveType(
                        createLeaveRequestDto.getLeaveType()
                );

        LeavePolicyEntity leavePolicy =
                leavePolicyService.findLeavePolicyOrThrow(
                        leaveType,
                        year
                );

        Integer noOfDays =
                calculateLeaveDays(
                        startDate,
                        endDate
                );

        LeaveBalanceEntity leaveBalance =
                leaveBalanceService.findLeaveBalanceOrThrow(
                        employee,
                        leavePolicy
                );

        validateLeaveBalance(
                leaveBalance,
                noOfDays
        );

        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(startDate);
        leaveRequest.setEndDate(endDate);
        leaveRequest.setNoOfDays(noOfDays);
        leaveRequest.setReason(createLeaveRequestDto.getReason());
        leaveRequest.setYear(year);
        leaveRequest.setSubmittedAt(LocalDateTime.now());

        return updateAndSaveLeaveStatus(
                leaveRequest,
                LeaveRequestStatusEnum.PENDING
        );
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public LeaveRequestProjectionDTO cancelLeaveRequest(
            Long leaveRequestId
    ) {

        UserEntity employee =
                userService.getAuthenticatedUser();

        userService.requireAssignedManager(employee);

        LeaveRequestEntity leaveRequest =
                findLeaveOrThrow(leaveRequestId);

        leaveRequestWorkflowService.validateTransition(
                leaveRequest,
                LeaveRequestActionEnum.CANCEL_REQUEST
        );

        return updateAndSaveLeaveStatus(
                leaveRequest,
                LeaveRequestStatusEnum.CANCELLED
        );
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public LeaveRequestProjectionDTO requestLeaveCancel(
            Long id
    ) {

        LeaveRequestEntity leaveRequest =
                findLeaveOrThrow(id);

        leaveRequestWorkflowService.validateTransition(
                leaveRequest,
                LeaveRequestActionEnum.REQUEST_CANCEL
        );

        return updateAndSaveLeaveStatus(
                leaveRequest,
                LeaveRequestStatusEnum.CANCEL_PENDING
        );
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public void deleteLeaveDraft(
            Long id
    ) {

        LeaveRequestEntity leaveRequest =
                findLeaveOrThrow(id);

        leaveRequestWorkflowService.validateTransition(
                leaveRequest,
                LeaveRequestActionEnum.DELETE_DRAFT
        );

        leaveRequestRepo.delete(leaveRequest);
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public LeaveRequestProjectionDTO approveLeaveRequest(
            Long id
    ) {

        LeaveRequestEntity leaveRequest =
                findLeaveOrThrow(id);

        UserEntity manager =
                userService.getAuthenticatedUser();

        userService.validateManager(
                leaveRequest,
                manager
        );

        leaveRequestWorkflowService.validateTransition(
                leaveRequest,
                LeaveRequestActionEnum.APPROVE_REQUEST
        );

        resolveLeaveBalanceAndSave(
                leaveRequest,
                LeaveRequestActionEnum.APPROVE_REQUEST
        );

        return updateAndSaveLeaveStatus(
                leaveRequest,
                LeaveRequestStatusEnum.APPROVED
        );
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public LeaveRequestProjectionDTO rejectLeaveRequest(
            Long id
    ) {

        LeaveRequestEntity leaveRequest =
                findLeaveOrThrow(id);

        UserEntity manager =
                userService.getAuthenticatedUser();

        userService.validateManager(
                leaveRequest,
                manager
        );

        leaveRequestWorkflowService.validateTransition(
                leaveRequest,
                LeaveRequestActionEnum.REJECT_REQUEST
        );

        return updateAndSaveLeaveStatus(
                leaveRequest,
                LeaveRequestStatusEnum.REJECTED
        );
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public LeaveRequestProjectionDTO approveCancelRequest(
            Long id
    ) {

        LeaveRequestEntity leaveRequest =
                findLeaveOrThrow(id);

        UserEntity manager =
                userService.getAuthenticatedUser();

        userService.validateManager(
                leaveRequest,
                manager
        );

        leaveRequestWorkflowService.validateTransition(
                leaveRequest,
                LeaveRequestActionEnum.APPROVE_CANCEL
        );

        resolveLeaveBalanceAndSave(
                leaveRequest,
                LeaveRequestActionEnum.APPROVE_CANCEL
        );

        return updateAndSaveLeaveStatus(
                leaveRequest,
                LeaveRequestStatusEnum.CANCELLED
        );
    }


    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public LeaveRequestProjectionDTO rejectCancelRequest(
            Long id
    ) {

        LeaveRequestEntity leaveRequest =
                findLeaveOrThrow(id);

        UserEntity manager =
                userService.getAuthenticatedUser();

        userService.validateManager(
                leaveRequest,
                manager
        );

        leaveRequestWorkflowService.validateTransition(
                leaveRequest,
                LeaveRequestActionEnum.REJECT_CANCEL
        );

        return updateAndSaveLeaveStatus(
                leaveRequest,
                LeaveRequestStatusEnum.APPROVED
        );
    }


    @Override
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<LeaveRequestProjectionDTO> getEmployeeLeaveRequests() {

        UserEntity employee =
                userService.getAuthenticatedUser();

        List<LeaveRequestEntity> leaveRequests =
                leaveRequestRepo.findByEmployee(employee);

        return leaveRequests.stream()
                .map(this::mapWithActions)
                .toList();
    }


    @Override
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<LeaveRequestProjectionDTO> getEmployeeLeaveDrafts() {

        UserEntity employee =
                userService.getAuthenticatedUser();

        List<LeaveRequestEntity> leaveRequests =
                leaveRequestRepo.findByEmployee(employee);

        return leaveRequests.stream()
                .filter(
                        leaveRequest ->
                                leaveRequest.getStatus()
                                        == LeaveRequestStatusEnum.DRAFT
                )
                .map(this::mapWithActions)
                .toList();
    }


    @Override
    @PreAuthorize("hasRole('MANAGER')")
    public List<ManagerEmployeeLeaveDTO> getLeaveRequests() {

        Long managerId =
                userService.getAuthenticatedUser().getId();

        List<LeaveRequestEntity> leaveRequests =
                leaveRequestRepo.findManagerEmployeeLeaveRequests(
                        managerId
                );

        return leaveRequests.stream()
                .map(
                        leaveRequest ->
                                new ManagerEmployeeLeaveDTO(
                                        mapWithActions(leaveRequest),
                                        leaveRequest.getEmployee().getName(),
                                        leaveRequest.getEmployee().getEmail()
                                )
                )
                .toList();
    }





    private void validateSubmissionPayload(
            CreateLeaveRequestDTO createLeaveRequestDto
    ) {

        if (
                createLeaveRequestDto.getLeaveType() == null ||
                        createLeaveRequestDto.getLeaveType().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Leave type should be specified."
            );
        }

        if (
                createLeaveRequestDto.getStartDate() == null ||
                        createLeaveRequestDto.getEndDate() == null
        ) {

            throw new IllegalArgumentException(
                    "Missing required date fields."
            );
        }

        if (
                createLeaveRequestDto.getEndDate()
                        .isBefore(
                                createLeaveRequestDto.getStartDate()
                        )
        ) {

            throw new IllegalArgumentException(
                    "Invalid date range."
            );
        }
    }


    private void validateNoticePeriod(
            LocalDate startDate
    ) {

        LocalDate today =
                LocalDate.now();

        if (startDate.isBefore(today)) {

            throw new IllegalStateException(
                    "Leave start date cannot be in the past."
            );
        }

        long noticeDays =
                ChronoUnit.DAYS.between(
                        today,
                        startDate
                );

        if (noticeDays < MINIMUM_NOTICE_DAYS) {

            throw new IllegalStateException(
                    "Leave requests must be submitted at least "
                            + MINIMUM_NOTICE_DAYS
                            + " days in advance."
            );
        }
    }


    private void validateLeaveBalance(
            LeaveBalanceEntity leaveBalance,
            Integer noOfDays
    ) {

        if (leaveBalance.getRemainingLeave() < noOfDays) {

            throw new IllegalStateException(
                    "Leave balance insufficient."
            );
        }
    }


    // =========================================================================
    // POLICY / BALANCE RESOLUTION
    // =========================================================================


    private void resolveLeaveBalanceAndSave(
            LeaveRequestEntity leaveRequest,
            LeaveRequestActionEnum action
    ) {

        LeavePolicyEntity leavePolicy =
                leavePolicyService.findLeavePolicyOrThrow(
                        leaveRequest.getLeaveType(),
                        leaveRequest.getYear()
                );

        LeaveBalanceEntity leaveBalance =
                leaveBalanceService.findLeaveBalanceOrThrow(
                        leaveRequest.getEmployee(),
                        leavePolicy
                );

        Integer noOfDaysRequested =
                leaveRequest.getNoOfDays();

        switch (action) {

            case APPROVE_REQUEST -> {

                validateLeaveBalance(
                        leaveBalance,
                        noOfDaysRequested
                );

                leaveBalance.setConsumedLeave(
                        leaveBalance.getConsumedLeave()
                                + noOfDaysRequested
                );

                leaveBalance.setRemainingLeave(
                        leaveBalance.getRemainingLeave()
                                - noOfDaysRequested
                );
            }

            case APPROVE_CANCEL -> {

                leaveBalance.setConsumedLeave(
                        leaveBalance.getConsumedLeave()
                                - noOfDaysRequested
                );

                leaveBalance.setRemainingLeave(
                        leaveBalance.getRemainingLeave()
                                + noOfDaysRequested
                );
            }
        }

        leaveBalance.setUpdatedAt(LocalDateTime.now());

        leaveBalanceRepo.save(leaveBalance);
    }


    // =========================================================================
    // PERSISTENCE HELPERS
    // =========================================================================


    private LeaveRequestEntity buildAndSaveLeaveRequest(
            UserEntity employee,
            LeaveTypeEntity leaveType,
            CreateLeaveRequestDTO createLeaveRequestDto,
            LeaveRequestStatusEnum status,
            Integer year,
            LocalDateTime submittedAt
    ) {

        LocalDate startDate =
                createLeaveRequestDto.getStartDate();

        LocalDate endDate =
                createLeaveRequestDto.getEndDate();

        Integer noOfDays =
                calculateLeaveDays(
                        startDate,
                        endDate
                );

        LeaveRequestEntity leaveRequest =
                LeaveRequestEntity.builder()
                        .employee(employee)
                        .leaveType(leaveType)
                        .startDate(startDate)
                        .endDate(endDate)
                        .noOfDays(noOfDays)
                        .reason(createLeaveRequestDto.getReason())
                        .status(status)
                        .createdAt(LocalDateTime.now())
                        .submittedAt(submittedAt)
                        .year(year)
                        .build();

        return leaveRequestRepo.save(leaveRequest);
    }


    private LeaveRequestProjectionDTO updateAndSaveLeaveStatus(
            LeaveRequestEntity leaveRequest,
            LeaveRequestStatusEnum newStatus
    ) {

        leaveRequest.setStatus(newStatus);

        LeaveRequestEntity savedLeave =
                leaveRequestRepo.save(leaveRequest);

        return mapWithActions(savedLeave);
    }


    // =========================================================================
    // ENTITY / RESOURCE RESOLUTION
    // =========================================================================


    private LeaveRequestEntity findLeaveOrThrow(
            Long id
    ) {

        return leaveRequestRepo.findById(id)
                .filter(
                        leaveRequest ->
                                leaveRequest.getStatus()
                                        != LeaveRequestStatusEnum.CANCELLED
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid or cancelled leave request."
                        )
                );
    }


    // =========================================================================
    // PROJECTION MAPPING
    // =========================================================================


    private LeaveRequestProjectionDTO mapWithActions(
            LeaveRequestEntity leaveRequest
    ) {

        LeaveRequestProjectionDTO response =
                leaveRequestMapper.mapToEmployeeLeaveResponse(
                        leaveRequest
                );

        response.setAllowedActions(
                leaveRequestWorkflowService.allowedLeaveActions(leaveRequest)
        );

        return response;
    }


    // =========================================================================
    // PURE UTILITIES
    // =========================================================================


    private Integer extractYear(
            LocalDate startDate
    ) {

        return startDate != null
                ? startDate.getYear()
                : null;
    }


    public Integer calculateLeaveDays(
            LocalDate startDate,
            LocalDate endDate
    ) {

        return (int)
                ChronoUnit.DAYS.between(
                        startDate,
                        endDate
                ) + 1;
    }
}