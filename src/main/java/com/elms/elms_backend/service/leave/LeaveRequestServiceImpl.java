package com.elms.elms_backend.service.leave;

import com.elms.elms_backend.dto.leave.CreateLeaveRequestDTO;
import com.elms.elms_backend.dto.leave.CreateLeaveRequestResponseDTO;
import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyDTO;
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
import java.util.Date;
import java.util.List;

@Service
public class LeaveRequestServiceImpl
        implements LeaveRequestService {

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
            LeaveRequestMapper leaveRequestMapper, LeavePolicyRepository leavePolicyRepo, LeaveBalanceRepository leaveBalanceRepo
    ) {

        this.leaveRequestRepo = leaveRequestRepo;
        this.userService = userService;
        this.leaveTypeService = leaveTypeService;
        this.leaveRequestMapper = leaveRequestMapper;
        this.leavePolicyRepo = leavePolicyRepo;

        this.leaveBalanceRepo = leaveBalanceRepo;
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
    public CreateLeaveRequestResponseDTO createLeaveDraft(
            CreateLeaveRequestDTO createLeaveRequestDto
    ) {

        LeaveTypeEntity leaveType =
                leaveTypeService
                        .resolveOptionalLeaveType(
                                createLeaveRequestDto.getLeaveType()
                        );

        UserEntity user =
                userService.getAuthenticatedUser();

        Integer year =
                extractYear(
                        createLeaveRequestDto.getStartDate()
                );

        LeaveRequestEntity request =
                LeaveRequestEntity.builder()
                        .user(user)
                        .leaveType(leaveType)
                        .startDate(
                                createLeaveRequestDto.getStartDate()
                        )
                        .endDate(
                                createLeaveRequestDto.getEndDate()
                        )
                        .reason(
                                createLeaveRequestDto.getReason()
                        )
                        .status(
                                LeaveRequestStatusEnum.DRAFT
                        )
                        .createdAt(LocalDateTime.now())
                        .submittedAt(null)
                        .year(year)
                        .build();

        LeaveRequestEntity savedLeave =
                leaveRequestRepo.save(request);

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
    public CreateLeaveRequestResponseDTO submitNewLeaveRequest(
            CreateLeaveRequestDTO createLeaveRequestDto
    ) {

        validateSubmissionPayload(createLeaveRequestDto);

        LeaveTypeEntity leaveType =
                leaveTypeService.resolveLeaveType(
                        createLeaveRequestDto.getLeaveType()
                );
        Integer year =
                extractYear(
                        createLeaveRequestDto.getStartDate()
                );
        LeavePolicyEntity leavePolicy =
                leavePolicyRepo.findByLeaveTypeAndYear(leaveType, year);

        UserEntity user =
                userService.getAuthenticatedUser();

        LeaveBalanceEntity leaveBalance = leaveBalanceRepo.findByUserAndLeavePolicy(user, leavePolicy);
        Integer noOfDaysRequested = calculateLeaveDays(createLeaveRequestDto);

        if (leaveBalance.getRemainingLeave() < noOfDaysRequested) {
            throw new IllegalStateException("Leave balance insufficient");
        }

        Integer consumedLeave = leaveBalance.getConsumedLeave() + noOfDaysRequested;
        Integer remainingLeave = leaveBalance.getRemainingLeave() - noOfDaysRequested;

        leaveBalance.setConsumedLeave(consumedLeave);
        leaveBalance.setRemainingLeave(remainingLeave);
        leaveBalance.setUpdatedAt(LocalDateTime.now());
        leaveBalanceRepo.save(leaveBalance);

        LeaveRequestEntity request =
                LeaveRequestEntity.builder()
                        .user(user)
                        .leaveType(leaveType)
                        .startDate(
                                createLeaveRequestDto.getStartDate()
                        )
                        .endDate(
                                createLeaveRequestDto.getEndDate()
                        )
                        .reason(
                                createLeaveRequestDto.getReason()
                        )
                        .status(
                                LeaveRequestStatusEnum.PENDING
                        )
                        .createdAt(LocalDateTime.now())
                        .submittedAt(LocalDateTime.now())
                        .year(year)
                        .build();

        LeaveRequestEntity savedLeave =
                leaveRequestRepo.save(request);

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
    public CreateLeaveRequestResponseDTO submitLeaveRequest(
            Long id,
            CreateLeaveRequestDTO createLeaveRequestDto
    ) {

        LeaveRequestEntity leaveRequest =
                leaveRequestRepo.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Leave not found"
                                )
                        );

        validateAction(
                leaveRequest,
                LeaveRequestActionEnum.SUBMIT
        );

        validateSubmissionPayload(
                createLeaveRequestDto
        );

        LeaveTypeEntity leaveType =
                leaveTypeService.resolveLeaveType(
                        createLeaveRequestDto.getLeaveType()
                );


        Integer year =
                extractYear(
                        createLeaveRequestDto.getStartDate()
                );


        LeavePolicyEntity leavePolicy =
                leavePolicyRepo.findByLeaveTypeAndYear(leaveType, year);

        UserEntity user =
                userService.getAuthenticatedUser();
        LeaveBalanceEntity leaveBalance = leaveBalanceRepo.findByUserAndLeavePolicy(user, leavePolicy);
        Integer noOfDaysRequested = calculateLeaveDays(createLeaveRequestDto);
        if (leaveBalance.getRemainingLeave() < noOfDaysRequested) {
            throw new IllegalStateException("Leave balance insufficient");
        }
        Integer consumedLeave = leaveBalance.getConsumedLeave() + noOfDaysRequested;
        Integer remainingLeave = leaveBalance.getRemainingLeave() - noOfDaysRequested;
        leaveBalance.setConsumedLeave(consumedLeave);
        leaveBalance.setRemainingLeave(remainingLeave);
        leaveBalance.setUpdatedAt(LocalDateTime.now());
        leaveBalanceRepo.save(leaveBalance);
        leaveRequest.setLeaveType(leaveType);

        leaveRequest.setStartDate(
                createLeaveRequestDto.getStartDate()
        );

        leaveRequest.setEndDate(
                createLeaveRequestDto.getEndDate()
        );

        leaveRequest.setReason(
                createLeaveRequestDto.getReason()
        );

        leaveRequest.setStatus(
                LeaveRequestStatusEnum.PENDING
        );

        leaveRequest.setSubmittedAt(
                LocalDateTime.now()
        );

        leaveRequest.setYear(year);

        LeaveRequestEntity savedLeave =
                leaveRequestRepo.save(leaveRequest);

        return mapWithActions(savedLeave);
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
    public CreateLeaveRequestResponseDTO requestLeaveCancel(
            Long id
    ) {

        LeaveRequestEntity leaveRequest =
                leaveRequestRepo.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid leave"
                                )
                        );

        validateAction(
                leaveRequest,
                LeaveRequestActionEnum.REQUEST_CANCEL
        );

        leaveRequest.setStatus(
                LeaveRequestStatusEnum.CANCEL_REQUESTED
        );

        LeaveRequestEntity savedLeave =
                leaveRequestRepo.save(leaveRequest);

        return mapWithActions(savedLeave);
    }


    /**
     * Approves pending leave request.
     *
     * @param id leave request identifier
     * @return approved leave projection
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @Transactional
    public CreateLeaveRequestResponseDTO approveLeaveRequest(
            Long id
    ) {

        LeaveRequestEntity leaveRequest =
                leaveRequestRepo.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid leave"
                                )
                        );

        validateAction(
                leaveRequest,
                LeaveRequestActionEnum.APPROVE_REQUEST
        );

        leaveRequest.setStatus(
                LeaveRequestStatusEnum.APPROVED
        );

        LeaveRequestEntity savedLeave =
                leaveRequestRepo.save(leaveRequest);

        return mapWithActions(savedLeave);
    }


    /**
     * Rejects pending leave request.
     *
     * @param id leave request identifier
     * @return rejected leave projection
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @Transactional
    public CreateLeaveRequestResponseDTO rejectLeaveRequest(
            Long id
    ) {

        LeaveRequestEntity leaveRequest =
                leaveRequestRepo.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid leave"
                                )
                        );

        validateAction(
                leaveRequest,
                LeaveRequestActionEnum.REJECT_REQUEST
        );

        leaveRequest.setStatus(
                LeaveRequestStatusEnum.REJECTED
        );

        LeaveRequestEntity savedLeave =
                leaveRequestRepo.save(leaveRequest);

        return mapWithActions(savedLeave);
    }


    /**
     * Approves leave cancellation request.
     *
     * @param id leave request identifier
     * @return cancelled leave projection
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @Transactional
    public CreateLeaveRequestResponseDTO approveCancelRequest(
            Long id
    ) {

        LeaveRequestEntity leaveRequest =
                leaveRequestRepo.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid leave"
                                )
                        );

        validateAction(
                leaveRequest,
                LeaveRequestActionEnum.APPROVE_CANCEL
        );

        leaveRequest.setStatus(
                LeaveRequestStatusEnum.CANCELLED
        );

        LeaveRequestEntity savedLeave =
                leaveRequestRepo.save(leaveRequest);

        return mapWithActions(savedLeave);
    }


    /**
     * Rejects leave cancellation request.
     *
     * @param id leave request identifier
     * @return restored approved leave projection
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @Transactional
    public CreateLeaveRequestResponseDTO rejectCancelRequest(
            Long id
    ) {

        LeaveRequestEntity leaveRequest =
                leaveRequestRepo.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid leave"
                                )
                        );

        validateAction(
                leaveRequest,
                LeaveRequestActionEnum.REJECT_CANCEL
        );

        leaveRequest.setStatus(
                LeaveRequestStatusEnum.APPROVED
        );

        LeaveRequestEntity savedLeave =
                leaveRequestRepo.save(leaveRequest);

        return mapWithActions(savedLeave);
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

        LeaveRequestEntity leaveRequest =
                leaveRequestRepo.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid leave"
                                )
                        );

        validateAction(
                leaveRequest,
                LeaveRequestActionEnum.DELETE_DRAFT
        );

        leaveRequestRepo.delete(leaveRequest);
    }


    /**
     * Fetches authenticated employee leave requests.
     *
     * @return employee leave request projections
     */
    @Override
    public List<CreateLeaveRequestResponseDTO>
    getEmployeeLeaveRequests() {

        UserEntity user =
                userService.getAuthenticatedUser();

        List<LeaveRequestEntity> leaves =
                leaveRequestRepo.findByUser(user);

        return leaves.stream()
                .map(this::mapWithActions)
                .toList();
    }


    /**
     * Fetches authenticated employee leave drafts.
     *
     * @return employee draft projections
     */
    @Override
    public List<CreateLeaveRequestResponseDTO>
    getEmployeeLeaveDrafts() {

        UserEntity user =
                userService.getAuthenticatedUser();

        List<LeaveRequestEntity> leaves =
                leaveRequestRepo.findByUser(user);

        return leaves.stream()
                .filter(leave ->
                        leave.getStatus()
                                ==
                                LeaveRequestStatusEnum.DRAFT
                )
                .map(this::mapWithActions)
                .toList();
    }


    /**
     * Resolves allowed workflow actions
     * for authenticated user.
     *
     * @param leaveRequest target leave request
     * @return allowed workflow actions
     */
    @Override
    public List<LeaveRequestActionEnum>
    allowedLeaveActions(
            LeaveRequestEntity leaveRequest
    ) {

        UserEntity user =
                userService.getAuthenticatedUser();

        RoleEnum userRole =
                user.getRole().getName();

        LeaveRequestStatusEnum status =
                leaveRequest.getStatus();

        boolean isOwner =
                user.getId().equals(
                        leaveRequest.getUser().getId()
                );

        List<LeaveRequestActionEnum> actions =
                new ArrayList<>();


        if (userRole == RoleEnum.EMPLOYEE) {

            if (isOwner &&
                    status ==
                            LeaveRequestStatusEnum.DRAFT) {

                actions.add(
                        LeaveRequestActionEnum.DELETE_DRAFT
                );

                actions.add(
                        LeaveRequestActionEnum.SAVE_DRAFT
                );

                actions.add(
                        LeaveRequestActionEnum.SUBMIT
                );
            }
            if (isOwner &&
                    status ==
                            LeaveRequestStatusEnum.PENDING) {
                actions.add(LeaveRequestActionEnum.CANCEL);
            }
            if (isOwner &&
                    status ==
                            LeaveRequestStatusEnum.APPROVED) {

                actions.add(
                        LeaveRequestActionEnum.REQUEST_CANCEL
                );
            }
        }


        if (userRole == RoleEnum.MANAGER) {

            if (status ==
                    LeaveRequestStatusEnum.PENDING) {

                actions.add(
                        LeaveRequestActionEnum.APPROVE_REQUEST
                );

                actions.add(
                        LeaveRequestActionEnum.REJECT_REQUEST
                );
            }

            if (status ==
                    LeaveRequestStatusEnum.CANCEL_REQUESTED) {

                actions.add(
                        LeaveRequestActionEnum.APPROVE_CANCEL
                );

                actions.add(
                        LeaveRequestActionEnum.REJECT_CANCEL
                );
            }
        }

        return actions;
    }


    /**
     * Validates whether workflow action
     * is currently permitted.
     *
     * @param leaveRequest target leave request
     * @param action       requested workflow action
     */
    private void validateAction(
            LeaveRequestEntity leaveRequest,
            LeaveRequestActionEnum action
    ) {

        List<LeaveRequestActionEnum> actions =
                allowedLeaveActions(leaveRequest);

        if (!actions.contains(action)) {

            throw new IllegalStateException(
                    "Unauthorized leave action"
            );
        }
    }


    /**
     * Maps leave response with
     * allowed workflow actions.
     *
     * @param leaveRequest target leave request
     * @return actionable leave projection
     */
    private CreateLeaveRequestResponseDTO mapWithActions(
            LeaveRequestEntity leaveRequest
    ) {

        CreateLeaveRequestResponseDTO response =
                leaveRequestMapper
                        .mapToResponse(leaveRequest);

        response.setAllowedActions(
                allowedLeaveActions(leaveRequest)
        );

        return response;
    }


    /**
     * Extracts operational year
     * from leave start date.
     *
     * @param startDate leave start date
     * @return extracted operational year
     */
    private Integer extractYear(
            LocalDate startDate
    ) {

        return startDate != null
                ? startDate.getYear()
                : null;
    }


    /**
     * Validates mandatory leave
     * submission payload fields.
     *
     * @param createLeaveRequestDto incoming leave payload
     */
    private void validateSubmissionPayload(
            CreateLeaveRequestDTO createLeaveRequestDto
    ) {

        if (createLeaveRequestDto.getLeaveType() == null
                || createLeaveRequestDto.getLeaveType().isBlank()) {

            throw new RuntimeException(
                    "Leave type should be specified"
            );
        }

        if (createLeaveRequestDto.getStartDate() == null
                || createLeaveRequestDto.getEndDate() == null) {

            throw new RuntimeException(
                    "Missing required date fields"
            );
        }

        if (createLeaveRequestDto.getEndDate()
                .isBefore(
                        createLeaveRequestDto.getStartDate()
                )) {

            throw new RuntimeException(
                    "Invalid date range"
            );
        }
    }

    /**
     * @param createLeaveRequestDTO incoming leave payload
     * @return no of leave days  requested
     */
    public Integer calculateLeaveDays(
            CreateLeaveRequestDTO createLeaveRequestDTO
    ) {
        return (int)
                ChronoUnit.DAYS.between(
                        createLeaveRequestDTO.getStartDate(),
                        createLeaveRequestDTO.getEndDate()
                ) + 1;
    }

}