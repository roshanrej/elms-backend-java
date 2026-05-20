package com.elms.elms_backend.service.leave;

import com.elms.elms_backend.dto.leave.LeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveResponseDTO;
import com.elms.elms_backend.entity.LeavePolicyEntity;
import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.LeaveActionEnum;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.mapper.leave.LeaveRequestMapper;
import com.elms.elms_backend.repository.leave.LeavePolicyRepository;
import com.elms.elms_backend.repository.leave.LeaveRequestRepository;
import com.elms.elms_backend.service.leavepolicy.LeavePolicyService;
import com.elms.elms_backend.service.leavetype.LeaveTypeService;
import com.elms.elms_backend.service.user.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeaveRequestServiceImpl
        implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepo;

    private final UserService userService;

    private final LeaveTypeService leaveTypeService;

    private final LeaveRequestMapper leaveRequestMapper;

    private final LeavePolicyRepository leavePolicyRepo;


    public LeaveRequestServiceImpl(
            LeaveRequestRepository leaveRequestRepo,
            UserService userService,
            LeaveTypeService leaveTypeService,
            LeaveRequestMapper leaveRequestMapper, LeavePolicyRepository leavePolicyRepo
    ) {

        this.leaveRequestRepo = leaveRequestRepo;
        this.userService = userService;
        this.leaveTypeService = leaveTypeService;
        this.leaveRequestMapper = leaveRequestMapper;
        this.leavePolicyRepo = leavePolicyRepo;

    }


    /**
     * Creates leave draft for authenticated employee.
     *
     * @param leaveRequestDto incoming leave draft payload
     * @return persisted leave draft projection
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Override
    public LeaveResponseDTO createLeaveDraft(
            LeaveRequestDTO leaveRequestDto
    ) {

        LeaveTypeEntity leaveType =
                leaveTypeService
                        .resolveOptionalLeaveType(
                                leaveRequestDto.getLeaveType()
                        );

        UserEntity user =
                userService.getAuthenticatedUser();

        Integer year =
                extractYear(
                        leaveRequestDto.getStartDate()
                );

        LeaveRequestEntity request =
                LeaveRequestEntity.builder()
                        .user(user)
                        .leaveType(leaveType)
                        .startDate(
                                leaveRequestDto.getStartDate()
                        )
                        .endDate(
                                leaveRequestDto.getEndDate()
                        )
                        .reason(
                                leaveRequestDto.getReason()
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
     * Gets total consumed leaves for leave type
     *
     * @param leaveTypeId id for leaveType
     * @return persisted number of leave days for type
     */
    @Override
    public Integer getTotalConsumedLeaves(Long leaveTypeId, Integer year) {

        return leaveRequestRepo.getTotalConsumedLeaves(leaveTypeId,year);
    }

    /**
     * Creates and submits new leave request.
     *
     * @param leaveRequestDto incoming leave submission payload
     * @return persisted submitted leave projection
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Override
    public LeaveResponseDTO submitNewLeaveRequest(
            LeaveRequestDTO leaveRequestDto
    ) {

        validateSubmissionPayload(leaveRequestDto);

        LeaveTypeEntity leaveType =
                leaveTypeService.resolveLeaveType(
                        leaveRequestDto.getLeaveType()
                );

//        LeavePolicyEntity leavePolicyEntity = leavePolicyRepo.findBy
        UserEntity user =
                userService.getAuthenticatedUser();

        Integer year =
                extractYear(
                        leaveRequestDto.getStartDate()
                );

        LeaveRequestEntity request =
                LeaveRequestEntity.builder()
                        .user(user)
                        .leaveType(leaveType)
                        .startDate(
                                leaveRequestDto.getStartDate()
                        )
                        .endDate(
                                leaveRequestDto.getEndDate()
                        )
                        .reason(
                                leaveRequestDto.getReason()
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
     * @param id leave request identifier
     * @param leaveRequestDto updated leave submission payload
     * @return submitted leave projection
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Override
    public LeaveResponseDTO submitLeaveRequest(
            Long id,
            LeaveRequestDTO leaveRequestDto
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
                LeaveActionEnum.SUBMIT
        );

        validateSubmissionPayload(
                leaveRequestDto
        );

        LeaveTypeEntity leaveType =
                leaveTypeService.resolveLeaveType(
                        leaveRequestDto.getLeaveType()
                );

        Integer year =
                extractYear(
                        leaveRequestDto.getStartDate()
                );

        leaveRequest.setLeaveType(leaveType);

        leaveRequest.setStartDate(
                leaveRequestDto.getStartDate()
        );

        leaveRequest.setEndDate(
                leaveRequestDto.getEndDate()
        );

        leaveRequest.setReason(
                leaveRequestDto.getReason()
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
    public LeaveResponseDTO requestLeaveCancel(
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
                LeaveActionEnum.REQUEST_CANCEL
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
    public LeaveResponseDTO approveLeaveRequest(
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
                LeaveActionEnum.APPROVE_REQUEST
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
    public LeaveResponseDTO rejectLeaveRequest(
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
                LeaveActionEnum.REJECT_REQUEST
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
    public LeaveResponseDTO approveCancelRequest(
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
                LeaveActionEnum.APPROVE_CANCEL
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
    public LeaveResponseDTO rejectCancelRequest(
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
                LeaveActionEnum.REJECT_CANCEL
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
                LeaveActionEnum.DELETE_DRAFT
        );

        leaveRequestRepo.delete(leaveRequest);
    }


    /**
     * Fetches authenticated employee leave requests.
     *
     * @return employee leave request projections
     */
    @Override
    public List<LeaveResponseDTO>
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
    public List<LeaveResponseDTO>
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
    public List<LeaveActionEnum>
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

        List<LeaveActionEnum> actions =
                new ArrayList<>();


        if(userRole == RoleEnum.EMPLOYEE) {

            if(isOwner &&
                    status ==
                            LeaveRequestStatusEnum.DRAFT) {

                actions.add(
                        LeaveActionEnum.DELETE_DRAFT
                );

                actions.add(
                        LeaveActionEnum.SAVE_DRAFT
                );

                actions.add(
                        LeaveActionEnum.SUBMIT
                );
            }
            if(isOwner &&
                    status ==
                            LeaveRequestStatusEnum.PENDING) {
                actions.add(LeaveActionEnum.CANCEL);
            }
            if(isOwner &&
                    status ==
                            LeaveRequestStatusEnum.APPROVED) {

                actions.add(
                        LeaveActionEnum.REQUEST_CANCEL
                );
            }
        }


        if(userRole == RoleEnum.MANAGER) {

            if(status ==
                    LeaveRequestStatusEnum.PENDING) {

                actions.add(
                        LeaveActionEnum.APPROVE_REQUEST
                );

                actions.add(
                        LeaveActionEnum.REJECT_REQUEST
                );
            }

            if(status ==
                    LeaveRequestStatusEnum.CANCEL_REQUESTED) {

                actions.add(
                        LeaveActionEnum.APPROVE_CANCEL
                );

                actions.add(
                        LeaveActionEnum.REJECT_CANCEL
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
     * @param action requested workflow action
     */
    private void validateAction(
            LeaveRequestEntity leaveRequest,
            LeaveActionEnum action
    ) {

        List<LeaveActionEnum> actions =
                allowedLeaveActions(leaveRequest);

        if(!actions.contains(action)) {

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
    private LeaveResponseDTO mapWithActions(
            LeaveRequestEntity leaveRequest
    ) {

        LeaveResponseDTO response =
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
     * @param leaveRequestDto incoming leave payload
     */
    private void validateSubmissionPayload(
            LeaveRequestDTO leaveRequestDto
    ) {

        if (leaveRequestDto.getLeaveType() == null
                || leaveRequestDto.getLeaveType().isBlank()) {

            throw new RuntimeException(
                    "Leave type should be specified"
            );
        }

        if (leaveRequestDto.getStartDate() == null
                || leaveRequestDto.getEndDate() == null) {

            throw new RuntimeException(
                    "Missing required date fields"
            );
        }

        if (leaveRequestDto.getEndDate()
                .isBefore(
                        leaveRequestDto.getStartDate()
                )) {

            throw new RuntimeException(
                    "Invalid date range"
            );
        }
    }
}