package com.elms.elms_backend.service.leave;

import com.elms.elms_backend.dto.leave.CreateLeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveRequestProjectionDTO;
import com.elms.elms_backend.dto.leave.ManagerEmployeeLeaveDTO;
import com.elms.elms_backend.entity.*;
import com.elms.elms_backend.entity.enums.LeaveRequestActionEnum;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.mapper.leave.LeaveRequestMapper;
import com.elms.elms_backend.repository.leave.LeaveBalanceRepository;
import com.elms.elms_backend.repository.leave.LeavePolicyRepository;
import com.elms.elms_backend.repository.leave.LeaveRequestRepository;
import com.elms.elms_backend.service.leavetype.LeaveTypeService;
import com.elms.elms_backend.service.user.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepo;
    private final UserService userService;
    private final LeaveTypeService leaveTypeService;
    private final LeaveRequestMapper leaveRequestMapper;
    private final LeavePolicyRepository leavePolicyRepo;
    private final LeaveBalanceRepository leaveBalanceRepo;

    public LeaveRequestServiceImpl(
            LeaveRequestRepository leaveRequestRepo,
            UserService userService,
            LeaveTypeService leaveTypeService,
            LeaveRequestMapper leaveRequestMapper,
            LeavePolicyRepository leavePolicyRepo,
            LeaveBalanceRepository leaveBalanceRepo) {

        this.leaveRequestRepo = leaveRequestRepo;
        this.userService = userService;
        this.leaveTypeService = leaveTypeService;
        this.leaveRequestMapper = leaveRequestMapper;
        this.leavePolicyRepo = leavePolicyRepo;
        this.leaveBalanceRepo = leaveBalanceRepo;

    }


    // -------------------------------------------------------------------------
    // Public API — override methods (signatures unchanged)
    // -------------------------------------------------------------------------


    /**
     * Cancels pending leave request  for authenticated employee.
     *
     * @param leaveRequestId incoming leave id
     * @return persisted leave request projection
     */

    @Override
    @Transactional
    public LeaveRequestProjectionDTO cancelLeaveRequest(Long leaveRequestId){
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(leaveRequestId);
        validateTransition(leaveRequest,LeaveRequestActionEnum.CANCEL_REQUEST);
        return updateAndSaveLeaveStatus(leaveRequest,LeaveRequestStatusEnum.CANCELLED);
    }

    /**
     * Creates leave draft for authenticated employee.
     *
     * @param createLeaveRequestDto incoming leave draft payload
     * @return persisted leave draft projection
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Override
    @Transactional
    public LeaveRequestProjectionDTO createLeaveDraft(CreateLeaveRequestDTO createLeaveRequestDto) {
        UserEntity employee = resolveAndValidateEmployee();
        LeaveTypeEntity leaveType = leaveTypeService.resolveOptionalLeaveType(createLeaveRequestDto.getLeaveType());
        Integer year = extractYear(createLeaveRequestDto.getStartDate());

        LeaveRequestEntity savedLeave = buildAndSaveLeaveRequest(
                employee, leaveType,
                createLeaveRequestDto,
                LeaveRequestStatusEnum.DRAFT,
                year,
                null   // submittedAt — drafts are not yet submitted
        );

        return mapWithActions(savedLeave);
    }


    /**
     * Creates and submits new leave request.
     *
     * @param createLeaveRequestDto incoming leave submission payload
     * @return persisted submitted leave projection
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Override
    @Transactional
    public LeaveRequestProjectionDTO submitNewLeaveRequest(CreateLeaveRequestDTO createLeaveRequestDto) {
        UserEntity employee = resolveAndValidateEmployee();
        validateSubmissionPayload(createLeaveRequestDto);
        LeaveTypeEntity leaveType = leaveTypeService.resolveLeaveType(createLeaveRequestDto.getLeaveType());
        Integer year = extractYear(createLeaveRequestDto.getStartDate());
        LeaveRequestEntity savedLeave = buildAndSaveLeaveRequest(
                employee, leaveType,
                createLeaveRequestDto,
                LeaveRequestStatusEnum.PENDING,
                year,
                LocalDateTime.now()
        );
        return mapWithActions(savedLeave);
    }


    /**
     * Submits existing leave draft.
     *
     * @param id                    leave request identifier
     * @param createLeaveRequestDto updated leave submission payload
     * @return submitted leave projection
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Override
    @Transactional
    public LeaveRequestProjectionDTO submitLeaveRequest(Long id, CreateLeaveRequestDTO createLeaveRequestDto) {
        resolveAndValidateEmployee();
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(id);

        validateSubmissionPayload(createLeaveRequestDto);
        LeaveTypeEntity leaveType = leaveTypeService.resolveLeaveType(createLeaveRequestDto.getLeaveType());
        validateTransition(leaveRequest, LeaveRequestActionEnum.SUBMIT_REQUEST);

        LocalDate startDate = createLeaveRequestDto.getStartDate();
        LocalDate endDate = createLeaveRequestDto.getEndDate();
        Integer noOfDays = calculateLeaveDays(startDate, endDate);
        Integer year = extractYear(startDate);

        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(startDate);
        leaveRequest.setEndDate(endDate);
        leaveRequest.setNoOfDays(noOfDays);
        leaveRequest.setReason(createLeaveRequestDto.getReason());
        leaveRequest.setYear(year);
        leaveRequest.setSubmittedAt(LocalDateTime.now());

        return updateAndSaveLeaveStatus(leaveRequest,LeaveRequestStatusEnum.PENDING);
    }


    /**
     * Requests cancellation for approved leave.
     *
     * @param id leave request identifier
     * @return updated leave projection
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Override
    @Transactional
    public LeaveRequestProjectionDTO requestLeaveCancel(Long id) {
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(id);
        validateTransition(leaveRequest, LeaveRequestActionEnum.REQUEST_CANCEL);
        return updateAndSaveLeaveStatus(leaveRequest, LeaveRequestStatusEnum.CANCEL_REQUESTED);
    }


    /**
     * Approves pending leave request.
     *
     * @param id leave request identifier
     * @return approved leave projection
     */
    @PreAuthorize("hasRole('MANAGER')")
    @Override
    @Transactional
    public LeaveRequestProjectionDTO approveLeaveRequest(Long id) {
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(id);
        validateTransition(leaveRequest, LeaveRequestActionEnum.APPROVE_REQUEST); // state machine
        resolveLeaveBalanceAndSave(leaveRequest,LeaveRequestActionEnum.APPROVE_REQUEST);
        return updateAndSaveLeaveStatus(leaveRequest,LeaveRequestStatusEnum.APPROVED);
    }


    /**
     * Rejects pending leave request.
     *
     * @param id leave request identifier
     * @return rejected leave projection
     */
    @PreAuthorize("hasRole('MANAGER')")
    @Override
    @Transactional
    public LeaveRequestProjectionDTO rejectLeaveRequest(Long id) {
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(id);
        validateTransition(leaveRequest, LeaveRequestActionEnum.REJECT_REQUEST);
        return updateAndSaveLeaveStatus(leaveRequest, LeaveRequestStatusEnum.REJECTED);
    }


    /**
     * Approves leave cancellation request.
     *
     * @param id leave request identifier
     * @return cancelled leave projection
     */
    @PreAuthorize("hasRole('MANAGER')")
    @Override
    @Transactional
    public LeaveRequestProjectionDTO approveCancelRequest(Long id) {
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(id);
        validateTransition(leaveRequest, LeaveRequestActionEnum.APPROVE_CANCEL);
        resolveLeaveBalanceAndSave(leaveRequest, LeaveRequestActionEnum.APPROVE_CANCEL);
        return updateAndSaveLeaveStatus(leaveRequest, LeaveRequestStatusEnum.CANCELLED);
    }


    /**
     * Rejects leave cancellation request.
     *
     * @param id leave request identifier
     * @return restored approved leave projection
     */
    @PreAuthorize("hasRole('MANAGER')")
    @Override
    @Transactional
    public LeaveRequestProjectionDTO rejectCancelRequest(Long id) {
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(id);
        validateTransition(leaveRequest, LeaveRequestActionEnum.REJECT_CANCEL);
        return updateAndSaveLeaveStatus(leaveRequest, LeaveRequestStatusEnum.APPROVED);
    }


    /**
     * Deletes employee leave draft.
     *
     * @param id leave request identifier
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Override
    @Transactional
    public void deleteLeaveDraft(Long id) {
        LeaveRequestEntity leaveRequest = findLeaveOrThrow(id);
        validateTransition(leaveRequest, LeaveRequestActionEnum.DELETE_DRAFT);
        leaveRequestRepo.delete(leaveRequest);
    }


    /**
     * Fetches leave requests for all employees under the authenticated manager.
     *
     * @return employee leave request projections
     */
    @PreAuthorize("hasRole('MANAGER')")
    @Override
    public List<ManagerEmployeeLeaveDTO> getLeaveRequests() {
        Long managerId = userService.getAuthenticatedUser().getId();
        List<LeaveRequestEntity> leaveRequests = leaveRequestRepo.findManagerEmployeeLeaveRequests(managerId);
        return leaveRequests.stream()
                .map(leaveRequest -> new ManagerEmployeeLeaveDTO(
                        mapWithActions(leaveRequest),
                        leaveRequest.getEmployee().getName(),
                        leaveRequest.getEmployee().getEmail()))
                .toList();
    }


    /**
     * Fetches all leave requests for the authenticated employee.
     *
     * @return employee leave request projections
     */
    @Override
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<LeaveRequestProjectionDTO> getEmployeeLeaveRequests() {
        UserEntity employee = userService.getAuthenticatedUser();
        List<LeaveRequestEntity> leaveRequests = leaveRequestRepo.findByEmployee(employee);
        return leaveRequests.stream().map(this::mapWithActions).toList();
    }


    /**
     * Fetches authenticated employee leave drafts.
     *
     * @return employee draft projections
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Override
    public List<LeaveRequestProjectionDTO> getEmployeeLeaveDrafts() {
        UserEntity employee = userService.getAuthenticatedUser();
        List<LeaveRequestEntity> leaves = leaveRequestRepo.findByEmployee(employee);
        return leaves.stream()
                .filter(leaveRequest -> leaveRequest.getStatus() == LeaveRequestStatusEnum.DRAFT)
                .map(this::mapWithActions)
                .toList();
    }


    /**
     * Resolves allowed workflow actions for the authenticated user.
     *
     * @param leaveRequest target leave request
     * @return allowed workflow actions
     */

    public List<LeaveRequestActionEnum> allowedLeaveActions(LeaveRequestEntity leaveRequest) {
        UserEntity user = userService.getAuthenticatedUser();
        RoleEnum userRole = user.getRole().getName();
        LeaveRequestStatusEnum status = leaveRequest.getStatus();
        boolean isOwner = user.getId().equals(leaveRequest.getEmployee().getId());

        List<LeaveRequestActionEnum> actions = new ArrayList<>();

        if (userRole == RoleEnum.EMPLOYEE) {
            if (isOwner && status == LeaveRequestStatusEnum.DRAFT) {
                actions.add(LeaveRequestActionEnum.DELETE_DRAFT);
                actions.add(LeaveRequestActionEnum.SUBMIT_REQUEST);
            }
            if (isOwner && status == LeaveRequestStatusEnum.PENDING) {
                actions.add(LeaveRequestActionEnum.CANCEL_REQUEST);
            }
            if (isOwner && status == LeaveRequestStatusEnum.APPROVED) {
                actions.add(LeaveRequestActionEnum.REQUEST_CANCEL);
            }
        }

        if (userRole == RoleEnum.MANAGER) {
            if (status == LeaveRequestStatusEnum.PENDING) {
                actions.add(LeaveRequestActionEnum.APPROVE_REQUEST);
                actions.add(LeaveRequestActionEnum.REJECT_REQUEST);
            }
            if (status == LeaveRequestStatusEnum.CANCEL_REQUESTED) {
                actions.add(LeaveRequestActionEnum.APPROVE_CANCEL);
                actions.add(LeaveRequestActionEnum.REJECT_CANCEL);
            }
        }

        return actions;
    }


    // -------------------------------------------------------------------------
    // Private utility methods
    // -------------------------------------------------------------------------


    /**
     * Returns the authenticated employee, asserting that a manager is assigned.
     * Centralises the repeated getAuthenticatedUser() + manager-null guard.
     *
     * @return authenticated employee entity
     * @throws IllegalStateException when employee has no assigned manager
     */
    private UserEntity resolveAndValidateEmployee() {
        UserEntity employee = userService.getAuthenticatedUser();
        if (employee.getManager() == null) {
            throw new IllegalStateException("Employee has no assigned manager.");
        }
        return employee;
    }


    /**
     * Looks up the leave balance for the given employee/policy pair, validates
     * that sufficient balance exists, and persists the deducted balance.
     * Centralises the repeated balance-fetch → validate → deduct → save block.
     *
     * @param leaveRequest persisted leave Request for the employee
     * @param action action to be done on the leave Request
     * @throws IllegalStateException when remaining leave is insufficient
     */
    private void resolveLeaveBalanceAndSave(
            LeaveRequestEntity leaveRequest,
            LeaveRequestActionEnum action) {

        LeavePolicyEntity leavePolicy = findLeavePolicyOrThrow(leaveRequest.getLeaveType(), leaveRequest.getYear());

        LeaveBalanceEntity leaveBalance = leaveBalanceRepo.findByEmployeeAndLeavePolicy(leaveRequest.getEmployee(), leavePolicy);

        int noOfDaysRequested = leaveRequest.getNoOfDays();

        switch(action){
            case APPROVE_REQUEST -> {if (leaveBalance.getRemainingLeave() < noOfDaysRequested) {
                throw new IllegalStateException("Leave balance insufficient");
            }
                leaveBalance.setConsumedLeave(leaveBalance.getConsumedLeave() + noOfDaysRequested);
                leaveBalance.setRemainingLeave(leaveBalance.getRemainingLeave() - noOfDaysRequested);
            }
            case APPROVE_CANCEL -> {
                leaveBalance.setConsumedLeave(leaveBalance.getConsumedLeave() - noOfDaysRequested);
                leaveBalance.setRemainingLeave(leaveBalance.getRemainingLeave() + noOfDaysRequested);
            }
        }
        leaveBalance.setUpdatedAt(LocalDateTime.now());
        leaveBalanceRepo.save(leaveBalance);
    }
    /**
     * Resolves the leave policy for the given leave type and year,
     * throwing a descriptive exception if none is configured.
     *
     * @param leaveType resolved leave type
     * @param year      operational year
     * @return matching leave policy entity
     * @throws IllegalStateException when no policy is configured for the combination
     */
    private LeavePolicyEntity findLeavePolicyOrThrow(LeaveTypeEntity leaveType, Integer year) {
        LeavePolicyEntity policy = leavePolicyRepo.findByLeaveTypeAndYear(leaveType, year);
        if (policy == null) {
            throw new IllegalStateException(
                    "No leave policy configured for leave type '" + leaveType.getName() + "' in year " + year + "."
            );
        }
        return policy;
    }

    /**
     * Builds a new {@link LeaveRequestEntity} from the supplied parameters and
     * persists it. Centralises the repeated builder → save pattern shared by
     * {@code createLeaveDraft} and {@code submitNewLeaveRequest}.
     *
     * @param employee              owning employee
     * @param leaveType             resolved leave type
     * @param createLeaveRequestDto incoming leave payload
     * @param status                initial status for the new request
     * @param year                  operational year
     * @param submittedAt           submission timestamp, or {@code null} for drafts
     * @return persisted leave request entity
     */
    private LeaveRequestEntity buildAndSaveLeaveRequest(
            UserEntity employee,
            LeaveTypeEntity leaveType,
            CreateLeaveRequestDTO createLeaveRequestDto,
            LeaveRequestStatusEnum status,
            Integer year,
            LocalDateTime submittedAt) {

        LocalDate startDate = createLeaveRequestDto.getStartDate();
        LocalDate endDate = createLeaveRequestDto.getEndDate();
        Integer noOfDays = calculateLeaveDays(startDate, endDate);
        LeaveRequestEntity request = LeaveRequestEntity.builder()
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

        return leaveRequestRepo.save(request);
    }


    /**
     * Applies a status transition to the given leave request and persists it,
     * then returns the updated projection. Centralises the repeated
     * setStatus → save → mapWithActions pattern used by leave transitions.
     *
     * @param leaveRequest target leave request
     * @param newStatus    status to transition to
     * @return updated leave projection with allowed actions
     */
    private LeaveRequestProjectionDTO updateAndSaveLeaveStatus(
            LeaveRequestEntity leaveRequest,
            LeaveRequestStatusEnum newStatus) {

        leaveRequest.setStatus(newStatus);
        LeaveRequestEntity savedLeave = leaveRequestRepo.save(leaveRequest);
        return mapWithActions(savedLeave);
    }


    /**
     * Fetches a leave request by id or throws a descriptive exception.
     * Centralises the repeated findById → orElseThrow pattern.
     *
     * @param id leave request identifier
     * @return found leave request entity
     * @throws RuntimeException when no leave request matches the given id
     */
    private LeaveRequestEntity findLeaveOrThrow(Long id) {
        return leaveRequestRepo.findById(id).filter(leaveRequest -> leaveRequest.getStatus() !=  LeaveRequestStatusEnum.CANCELLED)
                .orElseThrow(() -> new RuntimeException("Invalid or cancelled leave"));
    }


    /**
     * Validates whether the requested workflow transition is currently valid
     * for the target leave request resource.
     *
     * @param leaveRequest target leave request
     * @param action       requested workflow action
     */
    private void validateTransition(LeaveRequestEntity leaveRequest, LeaveRequestActionEnum action) {
        List<LeaveRequestActionEnum> actions = allowedLeaveActions(leaveRequest);
        if (!actions.contains(action)) {
            throw new IllegalStateException("You are not allowed to perform this action.");
        }

        LeaveRequestStatusEnum status = leaveRequest.getStatus();

        switch (action) {

            case SUBMIT_REQUEST -> {
                switch (status) {
                    case PENDING   -> throw new IllegalStateException("Leave request was already submitted.");
                    case APPROVED  -> throw new IllegalStateException("Approved leave requests cannot be resubmitted.");
                    case REJECTED  -> throw new IllegalStateException("Rejected leave requests cannot be resubmitted.");
                    case CANCELLED -> throw new IllegalStateException("Cancelled leave requests cannot be resubmitted.");
                }
            }

            case DELETE_DRAFT -> {
                if (status != LeaveRequestStatusEnum.DRAFT) {
                    throw new IllegalStateException("Only draft leave requests can be modified.");
                }
            }

            case CANCEL_REQUEST -> {
                if (status != LeaveRequestStatusEnum.PENDING) {
                    throw new IllegalStateException("Only pending leave requests can be cancelled.");
                }
            }

            case APPROVE_REQUEST -> {
                switch (status) {
                    case APPROVED          -> throw new IllegalStateException("Leave request was already approved.");
                    case REJECTED          -> throw new IllegalStateException("Leave request was already rejected.");
                    case CANCELLED         -> throw new IllegalStateException("Cancelled leave requests cannot be approved.");
                    case CANCEL_REQUESTED  -> throw new IllegalStateException("Leave request is currently awaiting cancellation approval.");
                }
            }

            case REJECT_REQUEST -> {
                switch (status) {
                    case APPROVED          -> throw new IllegalStateException("Approved leave requests cannot be rejected.");
                    case REJECTED          -> throw new IllegalStateException("Leave request was already rejected.");
                    case CANCELLED         -> throw new IllegalStateException("Cancelled leave requests cannot be rejected.");
                    case CANCEL_REQUESTED  -> throw new IllegalStateException("Leave request is currently awaiting cancellation approval.");
                }
            }

            case REQUEST_CANCEL -> {
                if (status != LeaveRequestStatusEnum.APPROVED) {
                    throw new IllegalStateException("Only approved leave requests can request cancellation.");
                }
            }

            case APPROVE_CANCEL -> {
                switch (status) {
                    case APPROVED  -> throw new IllegalStateException("Leave request cancellation was not requested.");
                    case CANCELLED -> throw new IllegalStateException("Leave request was already cancelled.");
                    case REJECTED  -> throw new IllegalStateException("Rejected leave requests cannot be cancelled.");
                }
            }

            case REJECT_CANCEL -> {
                switch (status) {
                    case APPROVED  -> throw new IllegalStateException("Leave request cancellation was not requested.");
                    case CANCELLED -> throw new IllegalStateException("Leave request already cancelled.");
                    case REJECTED  -> throw new IllegalStateException("Leave request already rejected.");
                }
            }
        }
    }


    /**
     * Maps a leave request to its projection DTO and attaches the allowed
     * workflow actions for the current authenticated user.
     *
     * @param leaveRequest target leave request
     * @return actionable leave projection
     */
    private LeaveRequestProjectionDTO mapWithActions(LeaveRequestEntity leaveRequest) {
        LeaveRequestProjectionDTO response = leaveRequestMapper.mapToEmployeeLeaveResponse(leaveRequest);
        response.setAllowedActions(allowedLeaveActions(leaveRequest));
        return response;
    }


    /**
     * Extracts the operational year from a leave start date.
     *
     * @param startDate leave start date
     * @return extracted year, or {@code null} if startDate is null
     */
    private Integer extractYear(LocalDate startDate) {
        return startDate != null ? startDate.getYear() : null;
    }


    /**
     * Validates mandatory fields required for submitting a leave request.
     *
     * @param createLeaveRequestDto incoming leave payload
     */
    private void validateSubmissionPayload(CreateLeaveRequestDTO createLeaveRequestDto) {
        if (createLeaveRequestDto.getLeaveType() == null || createLeaveRequestDto.getLeaveType().isBlank()) {
            throw new RuntimeException("Leave type should be specified");
        }
        if (createLeaveRequestDto.getStartDate() == null || createLeaveRequestDto.getEndDate() == null) {
            throw new RuntimeException("Missing required date fields");
        }
        if (createLeaveRequestDto.getEndDate().isBefore(createLeaveRequestDto.getStartDate())) {
            throw new RuntimeException("Invalid date range");
        }
    }


    /**
     * Calculates the number of leave days requested (inclusive of both endpoints).
     *
     * @param startDate start Date of leave
     * @param endDate end Date of leave
     * @return number of leave days requested
     */
    public Integer calculateLeaveDays(LocalDate startDate,LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate,
                endDate) + 1;
    }
}