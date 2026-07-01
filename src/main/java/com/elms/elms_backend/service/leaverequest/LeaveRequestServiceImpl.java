package com.elms.elms_backend.service.leaverequest;

import com.elms.elms_backend.dto.dashboard.ManagerDashboardProjectionDTO;
import com.elms.elms_backend.dto.leave.CreateLeaveRequestDTO;
import com.elms.elms_backend.dto.leave.EmployeeLeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveRequestProjectionDTO;
import com.elms.elms_backend.dto.leave.ManagerEmployeeLeaveDTO;
import com.elms.elms_backend.entity.*;
import com.elms.elms_backend.entity.enums.LeaveRequestActionEnum;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import com.elms.elms_backend.mapper.leave.LeaveRequestMapper;
import com.elms.elms_backend.repository.leave.LeaveBalanceRepository;
import com.elms.elms_backend.repository.leave.LeaveRequestRepository;
import com.elms.elms_backend.service.leaveauditlog.LeaveAuditLogService;
import com.elms.elms_backend.service.leavebalance.LeaveBalanceService;
import com.elms.elms_backend.service.leavepolicy.LeavePolicyService;
import com.elms.elms_backend.service.leaverequestworkflow.LeaveRequestWorkflowService;
import com.elms.elms_backend.service.leavetype.LeaveTypeService;
import com.elms.elms_backend.service.user.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {


    private final LeaveRequestRepository leaveRequestRepo;
    private final UserService userService;
    private final LeaveTypeService leaveTypeService;
    private final LeaveRequestMapper leaveRequestMapper;
    private final LeaveRequestWorkflowService leaveRequestWorkflowService;
    private final LeaveBalanceRepository leaveBalanceRepo;
    private final LeaveBalanceService leaveBalanceService;
    private final LeavePolicyService leavePolicyService;
    private final LeaveAuditLogService leaveAuditLogService;

    public LeaveRequestServiceImpl(
            LeaveRequestRepository leaveRequestRepo,
            UserService userService,
            LeaveTypeService leaveTypeService,
            LeaveRequestMapper leaveRequestMapper,
            LeaveRequestWorkflowService leaveRequestWorkflowService,
            LeaveBalanceRepository leaveBalanceRepo,
            LeaveBalanceService leaveBalanceService,
            LeavePolicyService leavePolicyService,
            LeaveAuditLogService leaveAuditLogService
    ) {
        this.leaveRequestRepo = leaveRequestRepo;
        this.userService = userService;
        this.leaveTypeService = leaveTypeService;
        this.leaveRequestMapper = leaveRequestMapper;
        this.leaveRequestWorkflowService = leaveRequestWorkflowService;
        this.leaveBalanceRepo = leaveBalanceRepo;
        this.leaveBalanceService = leaveBalanceService;
        this.leavePolicyService = leavePolicyService;
        this.leaveAuditLogService = leaveAuditLogService;
    }

    // =========================================================================
    // PUBLIC METHODS (Service Implementation)
    // =========================================================================

    /**
     * Creates a leave request resource in the 'DRAFT' state.
     * @param createLeaveRequestDto incoming leave draft payload
     *
     * @return persisted leave request projection
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public LeaveRequestProjectionDTO createLeaveDraft(CreateLeaveRequestDTO createLeaveRequestDto) {
        /**
         * Checks if authenticated employee has an assigned manager
         */
        UserEntity employee = userService.getAuthenticatedUser();
        leaveRequestWorkflowService.assertEmployeeCanSubmitLeave(employee);

        //Resolve optional leave type for draft
        LeaveTypeEntity leaveType = leaveTypeService.resolveOptionalLeaveType(createLeaveRequestDto.getLeaveType());

        /**
         * Builds and persists leave request  resource in 'DRAFT' state
         */
        LeaveRequestEntity savedLeave = buildAndSaveLeaveRequest(
                employee, leaveType, createLeaveRequestDto, LeaveRequestStatusEnum.DRAFT, null
        );
        // Records leave action
        leaveAuditLogService.recordLeaveAction(savedLeave, LeaveRequestActionEnum.SAVE_DRAFT, null);

        // Returns persisted leave request projection associated with possible actions
        // on the resource
        return mapWithActions(savedLeave);
    }

    /**
     * Submits a new leave request resource in the 'PENDING' state.
     *  @param createLeaveRequestDto incoming leave submission payload
     *
     * @return persisted leave request resource projection
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public LeaveRequestProjectionDTO submitNewLeaveRequest(CreateLeaveRequestDTO createLeaveRequestDto) {

        // Checks if authenticated employee has an assigned manager
        UserEntity employee = userService.getAuthenticatedUser();
        leaveRequestWorkflowService.assertEmployeeCanSubmitLeave(employee);
        //Validates submission payload for valid leave request submission
        validateSubmissionPayload(createLeaveRequestDto);

        LocalDate startDate = createLeaveRequestDto.getStartDate();
        LocalDate endDate = createLeaveRequestDto.getEndDate();

        Integer year = extractYear(startDate);

        //Resolve valid leave type
        LeaveTypeEntity leaveType = leaveTypeService.resolveLeaveType(createLeaveRequestDto.getLeaveType());
        //Resolves valid leave type
        LeavePolicyEntity leavePolicy = leavePolicyService.findLeavePolicyOrThrow(leaveType, year);
        // Implements leave policies on request
        leavePolicyService.validateLeaveDates(startDate, endDate);
        leavePolicyService.validateNoticePeriod(leavePolicy, startDate);
        Integer noOfDays = leavePolicyService.calculateLeaveDays(startDate, endDate);
        //Resolves leavebalance against leave policy
        LeaveBalanceEntity leaveBalance = leaveBalanceService.findLeaveBalanceOrThrow(employee, leavePolicy);
        // Checks for sufficient leave balance
        validateLeaveBalance(leaveBalance, noOfDays);

        LeaveRequestEntity savedLeave = buildAndSaveLeaveRequest(
                employee, leaveType, createLeaveRequestDto, LeaveRequestStatusEnum.PENDING, LocalDateTime.now()
        );

        leaveAuditLogService.recordLeaveAction(savedLeave, LeaveRequestActionEnum.SUBMIT_REQUEST, null);
        return mapWithActions(savedLeave);
    }

    /**
     * Submits an existing draft leave request, changing its state to 'PENDING'.
     * * @param id leave request identifier
     *
     * @return persisted leave request projection
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public LeaveRequestProjectionDTO submitLeaveRequest(Long id) {
        UserEntity employee = userService.getAuthenticatedUser();
        leaveRequestWorkflowService.assertEmployeeCanSubmitLeave(employee);
        /**
         *  Pre-Requisites existing leave request resource,
         *  complete data and
         *  valid state transition
         */
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(id);
        validateDraftCompleteness(leaveRequest);
        leaveRequestWorkflowService.validateTransition(leaveRequest, LeaveRequestActionEnum.SUBMIT_REQUEST);

        LocalDate startDate = leaveRequest.getStartDate();
        LocalDate endDate = leaveRequest.getEndDate();

        Integer year = extractYear(startDate);
        LeavePolicyEntity leavePolicy = leavePolicyService.findLeavePolicyOrThrow(
                leaveRequest.getLeaveType(), year
        );
        /**
         * Policy implementation for state transition
         */
        leavePolicyService.validateLeaveDates(startDate, endDate);
        leavePolicyService.validateNoticePeriod(leavePolicy, startDate);
        Integer noOfDays = leavePolicyService.calculateLeaveDays(startDate, endDate);

        //Set recalculated no of days
        leaveRequest.setNoOfDays(noOfDays);

        /**
         * Check sufficient leave balance against existing leave policy
         */
        LeaveBalanceEntity leaveBalance = leaveBalanceService.findLeaveBalanceOrThrow(employee, leavePolicy);
        validateLeaveBalance(leaveBalance, noOfDays);

        // Set submission date
        leaveRequest.setSubmittedAt(LocalDateTime.now());
        /**
         * Transition leave request state to 'PENDING'
         * Persist checked resource
         * Audit leave action
         */
        return transitionAndAudit(leaveRequest, LeaveRequestActionEnum.SUBMIT_REQUEST, LeaveRequestStatusEnum.PENDING);
    }

    /**
     * Fetches active leave requests for the authenticated employee.
     *
     * @return list of active employee leave requests
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<EmployeeLeaveRequestDTO> getEmployeeActiveLeaveRequests() {
        UserEntity employee = userService.getAuthenticatedUser();
        return leaveRequestRepo.findEmployeeRequestsWithStatuses(
                employee,
                List.of(LeaveRequestStatusEnum.PENDING, LeaveRequestStatusEnum.APPROVED, LeaveRequestStatusEnum.CANCEL_PENDING)
        ).stream().map(leaveRequestMapper::mapToEmployeeLeaveRequestDTO).toList();
    }

    /**
     * Edits an existing leave draft and persists it.
     * * @param id persisted leave draft id
     *
     * @param createLeaveRequestDTO updated payload
     * @return persisted leave request projection
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public LeaveRequestProjectionDTO editLeaveDraft(Long id, CreateLeaveRequestDTO createLeaveRequestDTO) {
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(id);
        leaveRequestWorkflowService.validateTransition(leaveRequest, LeaveRequestActionEnum.EDIT_DRAFT);

        LocalDate startDate = createLeaveRequestDTO.getStartDate();
        LocalDate endDate = createLeaveRequestDTO.getEndDate();
        leaveRequest.setStartDate(startDate);
        leaveRequest.setEndDate(endDate);
        leaveRequest.setYear(extractYear(startDate));

        if (startDate != null && endDate != null) {
            leaveRequest.setNoOfDays(
                    leavePolicyService.calculateLeaveDays(startDate, endDate)
            );
        }
        leaveRequest.setLeaveType(leaveTypeService.resolveOptionalLeaveType(createLeaveRequestDTO.getLeaveType()));
        leaveRequest.setReason(createLeaveRequestDTO.getReason());
        return transitionAndAudit(leaveRequest, LeaveRequestActionEnum.EDIT_DRAFT, LeaveRequestStatusEnum.DRAFT);
    }

    /**
     * Cancels a leave request currently in the 'PENDING' state.
     * * @param leaveRequestId leave request identifier
     *
     * @return persisted leave request projection
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public LeaveRequestProjectionDTO cancelLeaveRequest(Long leaveRequestId) {
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(leaveRequestId);
        leaveRequestWorkflowService.validateTransition(leaveRequest, LeaveRequestActionEnum.CANCEL_REQUEST);

        return transitionAndAudit(leaveRequest, LeaveRequestActionEnum.CANCEL_REQUEST, LeaveRequestStatusEnum.CANCELLED);
    }

    /**
     * Requests a cancellation for an 'APPROVED' leave request resource.
     * * @param id leave request identifier
     *
     * @return persisted leave request projection
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public LeaveRequestProjectionDTO requestLeaveCancel(Long id) {
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(id);
        leaveRequestWorkflowService.validateTransition(leaveRequest, LeaveRequestActionEnum.REQUEST_CANCEL);
        return transitionAndAudit(leaveRequest, LeaveRequestActionEnum.REQUEST_CANCEL, LeaveRequestStatusEnum.CANCEL_PENDING);
    }

    /**
     * Deletes a leave request resource in the 'DRAFT' state.
     * * @param id leave request identifier
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('EMPLOYEE')")
    public void deleteLeaveDraft(Long id) {
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(id);
        leaveRequestWorkflowService.validateTransition(leaveRequest, LeaveRequestActionEnum.DELETE_DRAFT);
        transitionAndAudit(leaveRequest, LeaveRequestActionEnum.DELETE_DRAFT, LeaveRequestStatusEnum.DELETED);
    }

    /**
     * Approves a leave request resource in the 'PENDING' state.
     * * @param id leave request identifier
     *
     * @return persisted leave request resource projection
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public LeaveRequestProjectionDTO approveLeaveRequest(Long id) {
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(id);
        UserEntity manager = userService.getAuthenticatedUser();
        leaveRequestWorkflowService.validateTransition(leaveRequest, LeaveRequestActionEnum.APPROVE_REQUEST);
        leaveRequestWorkflowService.assertManagerCanPerformAction(leaveRequest);

        resolveLeaveBalanceAndSave(leaveRequest, LeaveRequestActionEnum.APPROVE_REQUEST);

        return transitionAndAudit(leaveRequest, LeaveRequestActionEnum.APPROVE_REQUEST, LeaveRequestStatusEnum.APPROVED);
    }

    /**
     * Rejects a leave request resource in the 'PENDING' state.
     * * @param id leave request identifier
     *
     * @return persisted leave request resource projection
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public LeaveRequestProjectionDTO rejectLeaveRequest(Long id) {
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(id);
        UserEntity manager = userService.getAuthenticatedUser();
        leaveRequestWorkflowService.validateTransition(leaveRequest, LeaveRequestActionEnum.REJECT_REQUEST);

        leaveRequestWorkflowService.assertManagerCanPerformAction(leaveRequest);
        return transitionAndAudit(leaveRequest, LeaveRequestActionEnum.REJECT_REQUEST, LeaveRequestStatusEnum.REJECTED);
    }

    /**
     * Approves a leave request cancellation from the 'CANCEL_PENDING' state.
     * * @param id leave request identifier
     *
     * @return persisted leave request resource projection
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public LeaveRequestProjectionDTO approveCancelRequest(Long id) {
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(id);

        leaveRequestWorkflowService.validateTransition(leaveRequest, LeaveRequestActionEnum.APPROVE_CANCEL);
        leaveRequestWorkflowService.assertManagerCanPerformAction(leaveRequest);

        resolveLeaveBalanceAndSave(leaveRequest, LeaveRequestActionEnum.APPROVE_CANCEL);

        return transitionAndAudit(leaveRequest, LeaveRequestActionEnum.APPROVE_CANCEL, LeaveRequestStatusEnum.CANCELLED);
    }

    /**
     * Rejects a leave request cancellation in the 'CANCEL_PENDING' state.
     * * @param id leave request identifier
     *
     * @return persisted leave request resource projection
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public LeaveRequestProjectionDTO rejectCancelRequest(Long id) {
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(id);
        UserEntity manager = userService.getAuthenticatedUser();
        leaveRequestWorkflowService.validateTransition(leaveRequest, LeaveRequestActionEnum.REJECT_CANCEL);
        leaveRequestWorkflowService.assertManagerCanPerformAction(leaveRequest);

        return transitionAndAudit(leaveRequest, LeaveRequestActionEnum.REJECT_CANCEL, LeaveRequestStatusEnum.APPROVED);
    }

    /**
     * Returns all persisted leave requests for the authenticated employee.
     * * @return list of persisted leave request projections
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<LeaveRequestProjectionDTO> getEmployeeLeaveRequests() {
        UserEntity employee = userService.getAuthenticatedUser();
        return leaveRequestRepo
                .findEmployeeRequestsExcludingStatuses(employee, List.of(LeaveRequestStatusEnum.DRAFT, LeaveRequestStatusEnum.DELETED))
                .stream()
                .map(this::mapWithActions)
                .toList();
    }

    /**
     * Returns all persisted leave drafts for the authenticated employee.
     * * @return list of draft leave request projections
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<LeaveRequestProjectionDTO> getEmployeeLeaveDrafts() {
        UserEntity employee = userService.getAuthenticatedUser();
        return leaveRequestRepo
                .findEmployeeRequestsWithStatuses(employee, List.of(LeaveRequestStatusEnum.DRAFT))
                .stream()
                .map(this::mapWithActions)
                .toList();
    }

    /**
     * Returns persisted leave requests owned by the manager's reports.
     *  @return list of leave request projections for manager
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('MANAGER')")
    public List<ManagerEmployeeLeaveDTO> getManagerOwnedLeaveRequests() {
        Long managerId = userService.getAuthenticatedUser().getId();
        return leaveRequestRepo.findLeaveRequestsByManagerAndStatusIn(managerId, List.of(LeaveRequestStatusEnum.APPROVED, LeaveRequestStatusEnum.CANCEL_PENDING, LeaveRequestStatusEnum.PENDING, LeaveRequestStatusEnum.REJECTED)).stream()
                .map(lr -> new ManagerEmployeeLeaveDTO(
                        mapWithActions(lr),
                        lr.getEmployee().getName(),
                        lr.getEmployee().getEmail()
                )).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('MANAGER')")
    public ManagerDashboardProjectionDTO getManagerDashboardProjection() {
        Long managerId = userService.getAuthenticatedUser().getId();
        Integer pendingCount = leaveRequestRepo.countByManagerIdAndStatus(managerId,LeaveRequestStatusEnum.PENDING);
        Integer pendingCancelCount = leaveRequestRepo.countByManagerIdAndStatus(managerId, LeaveRequestStatusEnum.CANCEL_PENDING);
        List<ManagerDashboardProjectionDTO.ManagerDashboardLeaveProjectionDTO> upcomingApprovedLeaves = leaveRequestRepo.findUpcomingApprovedLeaves(
                managerId,
                LocalDate.now(),
                PageRequest.of(0,5)
        ).stream().map(
                lr->
                        leaveRequestMapper.mapToManagerDashboardLeaveProjection(lr)
        ).toList();
    return new ManagerDashboardProjectionDTO(upcomingApprovedLeaves,pendingCount,pendingCancelCount);
}

    // =========================================================================
    // PRIVATE METHODS (Helpers, Validations, and Utilities)
    // =========================================================================

    /**
     * Validates a persisted draft leave request resource for submission readiness.
     * * @param leaveRequest the draft to validate
     */
    private void validateDraftCompleteness(LeaveRequestEntity leaveRequest) {
        if (leaveRequest.getLeaveType() == null) {
            throw new IllegalArgumentException("Leave type is required before submission.");
        }
        if (leaveRequest.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required before submission.");
        }
        if (leaveRequest.getEndDate() == null) {
            throw new IllegalArgumentException("End date is required before submission.");
        }
        if (leaveRequest.getReason() == null || leaveRequest.getReason().isBlank()) {
            throw new IllegalArgumentException("Reason is required before submission.");
        }
        if (leaveRequest.getNoOfDays() == null || leaveRequest.getNoOfDays() <= 0) {
            throw new IllegalArgumentException("Leave duration must be valid before submission.");
        }

    }

    /**
     * Validates the incoming submission payload for a leave request.
     * * @param createLeaveRequestDto the DTO to validate
     *
     * @throws IllegalArgumentException if the payload is invalid
     */
    private void validateSubmissionPayload(CreateLeaveRequestDTO createLeaveRequestDto) {
        if (createLeaveRequestDto.getLeaveType() == null || createLeaveRequestDto.getLeaveType().isBlank()) {
            throw new IllegalArgumentException("Leave type should be specified.");
        }
        if (createLeaveRequestDto.getStartDate() == null || createLeaveRequestDto.getEndDate() == null) {
            throw new IllegalArgumentException("Missing required date fields.");
        }
        if (createLeaveRequestDto.getEndDate().isBefore(createLeaveRequestDto.getStartDate())) {
            throw new IllegalArgumentException("Invalid date range.");
        }
        if (createLeaveRequestDto.getReason() == null || createLeaveRequestDto.getReason().isBlank()) {
            throw new IllegalArgumentException("Reason must be specified.");
        }
    }


    private void validateLeaveBalance(
            LeaveBalanceEntity leaveBalance,
            Integer noOfDays
    ) {

        UserEntity employee =
                leaveBalance.getEmployee();

        LeavePolicyEntity leavePolicy =
                leaveBalance.getLeavePolicy();

        LeaveTypeEntity leaveType =
                leavePolicy.getLeaveType();

        Integer consumedLeaveDaysForPolicy =
                leaveRequestRepo
                        .sumLeaveDaysByEmployeeAndLeaveTypeAndStatusIn(
                                employee,
                                leaveType,
                                List.of(
                                        LeaveRequestStatusEnum.APPROVED,
                                        LeaveRequestStatusEnum.PENDING,
                                        LeaveRequestStatusEnum.CANCEL_PENDING
                                )
                        );

        int projectedUsage =
                consumedLeaveDaysForPolicy + noOfDays;

        if (projectedUsage > leavePolicy.getAllocatedLeave()) {
            throw new IllegalStateException(
                    "Leave balance insufficient for leave application."
            );
        }
    }

    /**
     * Applies leave balance effects upon manager actions
     *  APPROVE_REQUEST:
     *        consumes remaining leave balance.
     *  APPROVE_CANCEL:
     *       restores remaining leave balance.
     * @param leaveRequest resource acted upon
     * @param action the action taken
     * @throws IllegalArgumentException if leave balance insufficient for
     * requested leave duration
     */
    private void resolveLeaveBalanceAndSave(LeaveRequestEntity leaveRequest, LeaveRequestActionEnum action) {

        LeavePolicyEntity leavePolicy = leavePolicyService.findLeavePolicyOrThrow(
                leaveRequest.getLeaveType(), extractYear(leaveRequest.getStartDate())
        );

        LeaveBalanceEntity leaveBalance = leaveBalanceService.findLeaveBalanceOrThrow(
                leaveRequest.getEmployee(), leavePolicy
        );

        Integer noOfDays = leaveRequest.getNoOfDays();

        switch (action) {
            case APPROVE_REQUEST -> {
                if(leaveBalance.getRemainingLeave() < noOfDays){
                    throw new IllegalArgumentException("Leave balance insufficient for leave application");
                }
                leaveBalance.setConsumedLeave(leaveBalance.getConsumedLeave() + noOfDays);
                leaveBalance.setRemainingLeave(leaveBalance.getRemainingLeave() - noOfDays);
            }
            case APPROVE_CANCEL -> {
                leaveBalance.setConsumedLeave(leaveBalance.getConsumedLeave() - noOfDays);
                leaveBalance.setRemainingLeave(leaveBalance.getRemainingLeave() + noOfDays);
            }
        }

        leaveBalance.setUpdatedAt(LocalDateTime.now());
        leaveBalanceRepo.save(leaveBalance);
    }

    /**
     * Builds and persists the leave request resource.
     * * @param employee authenticated employee
     *
     * @param leaveType   leave type entity
     * @param dto         incoming payload
     * @param status      status of the leave request
     * @param submittedAt submission timestamp
     * @return persisted entity
     */
    private LeaveRequestEntity buildAndSaveLeaveRequest(
            UserEntity employee, LeaveTypeEntity leaveType, CreateLeaveRequestDTO dto,
            LeaveRequestStatusEnum status, LocalDateTime submittedAt
    ) {
        LocalDate startDate = dto.getStartDate();
        LocalDate endDate = dto.getEndDate();
        Integer noOfDays = leavePolicyService.calculateLeaveDays(startDate, endDate);

        LeaveRequestEntity leaveRequest = LeaveRequestEntity.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(startDate)
                .endDate(endDate)
                .noOfDays(noOfDays)
                .reason(dto.getReason())
                .status(status)
                .year(extractYear(startDate))
                .createdAt(LocalDateTime.now())
                .submittedAt(submittedAt)
                .build();

        return leaveRequestRepo.save(leaveRequest);
    }

    /**
     * Captures the previous status, saves the new status, records an audit, and returns the mapped projection.
     * * @param leaveRequest persisted leave request
     *
     * @param action    action performed
     * @param newStatus intended state transition status
     * @return persisted projection DTO
     */
    private LeaveRequestProjectionDTO transitionAndAudit(
            LeaveRequestEntity leaveRequest, LeaveRequestActionEnum action, LeaveRequestStatusEnum newStatus
    ) {
        LeaveRequestStatusEnum previousStatus = leaveRequest.getStatus();
        leaveRequest.setStatus(newStatus);
        LeaveRequestEntity savedLeave = leaveRequestRepo.save(leaveRequest);

        leaveAuditLogService.recordLeaveAction(savedLeave, action, previousStatus);
        return mapWithActions(savedLeave);
    }

    /**
     * Finds a non-cancelled,non-deleted leave request entity or throws an exception.
     * * @param id leave request identifier
     *
     * @return persisted leave request entity
     * @throws IllegalArgumentException if the request is not found or is 'CANCELLED'
     */
    private LeaveRequestEntity findLeaveOrThrow(Long id) {
        return leaveRequestRepo.findByIdWithDetails(id)
                .filter(lr ->
                        lr.getStatus() != LeaveRequestStatusEnum.CANCELLED
                                && lr.getStatus() != LeaveRequestStatusEnum.DELETED
                )
                .orElseThrow(() -> new IllegalArgumentException("Invalid or cancelled leave request."));
    }

    /**
     * Maps the entity to a projection and binds possible user interaction actions.
     * * @param leaveRequest persisted leave request
     *
     * @return projection DTO with bound allowed actions
     */
    private LeaveRequestProjectionDTO mapWithActions(LeaveRequestEntity leaveRequest) {
        LeaveRequestProjectionDTO response = leaveRequestMapper.mapToEmployeeLeaveResponse(leaveRequest);
        response.setAllowedActions(leaveRequestWorkflowService.allowedLeaveActions(leaveRequest));
        return response;
    }

    /**
     * Extracts the year from a given date.
     * * @param startDate the local date to extract from
     *
     * @return the year, or null if date is null
     */
    private Integer extractYear(LocalDate startDate) {

        return startDate != null ? startDate.getYear() : null;
    }

}