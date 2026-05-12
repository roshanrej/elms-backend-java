package com.elms.elms_backend.service.leave;

import com.elms.elms_backend.dto.leave.LeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveResponseDTO;
import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import com.elms.elms_backend.mapper.leave.LeaveRequestMapper;
import com.elms.elms_backend.repository.leave.LeaveRequestRepository;
import com.elms.elms_backend.service.leavetype.LeaveTypeService;
import com.elms.elms_backend.service.user.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepo;
    private final UserService userService;
    private final LeaveTypeService leaveTypeService;
    private final LeaveRequestMapper leaveRequestMapper;

    public LeaveRequestServiceImpl(
            LeaveRequestRepository leaveRequestRepo,
            UserService userService,
            LeaveTypeService leaveTypeService, LeaveRequestMapper leaveRequestMapper
    ) {
        this.leaveRequestRepo = leaveRequestRepo;
        this.userService = userService;
        this.leaveTypeService = leaveTypeService;
        this.leaveRequestMapper = leaveRequestMapper;
    }

    /**
     * Creates a leave request in DRAFT state.
     * <p>
     * Drafts are allowed to contain incomplete data and
     * do not enter approval workflow until explicitly submitted.
     *
     * @param leaveRequestDto incoming draft payload
     * @return persisted draft response
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Override
    public LeaveResponseDTO createLeaveDraft(
            LeaveRequestDTO leaveRequestDto
    ) {

        LeaveTypeEntity leaveType = leaveTypeService.resolveOptionalLeaveType(
                leaveRequestDto.getLeaveType()
        );

        UserEntity user = userService.getAuthenticatedUser();

        Integer year = extractYear(
                leaveRequestDto.getStartDate()
        );

        LeaveRequestEntity request = LeaveRequestEntity.builder()
                .user(user)
                .leaveType(leaveType)
                .startDate(leaveRequestDto.getStartDate())
                .endDate(leaveRequestDto.getEndDate())
                .reason(leaveRequestDto.getReason())
                .status(LeaveRequestStatusEnum.DRAFT)
                .createdAt(LocalDateTime.now())
                .submittedAt(null)
                .year(year)
                .build();

        LeaveRequestEntity savedLeave =
                leaveRequestRepo.save(request);

        return leaveRequestMapper.mapToResponse(savedLeave);
    }

    /**
     * Creates and submits a new leave request directly into
     * approval workflow.
     * <p>
     * Submitted requests must satisfy all mandatory validations.
     *
     * @param leaveRequestDto incoming leave request payload
     * @return persisted submitted leave response
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


        UserEntity user = userService.getAuthenticatedUser();

        Integer year = extractYear(
                leaveRequestDto.getStartDate()
        );

        LeaveRequestEntity request = LeaveRequestEntity.builder()
                .user(user)
                .leaveType(leaveType)
                .startDate(leaveRequestDto.getStartDate())
                .endDate(leaveRequestDto.getEndDate())
                .reason(leaveRequestDto.getReason())
                .status(LeaveRequestStatusEnum.PENDING)
                .createdAt(LocalDateTime.now())
                .submittedAt(LocalDateTime.now())
                .year(year)
                .build();

        LeaveRequestEntity savedLeave =
                leaveRequestRepo.save(request);

        return leaveRequestMapper.mapToResponse(savedLeave);
    }

    /**
     * Extracts year from leave start date.
     *
     * @param startDate leave start date
     * @return extracted year or null
     */
    private Integer extractYear(
            java.time.LocalDate startDate
    ) {
        return startDate != null
                ? startDate.getYear()
                : null;
    }

    /**
     * Validates mandatory submission fields
     * before workflow entry.
     *
     * @param leaveRequestDto incoming request payload
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
                .isBefore(leaveRequestDto.getStartDate())) {

            throw new RuntimeException(
                    "Invalid date range"
            );
        }
    }

    /**
     * Submits existing leave draft.
     *
     * @param id              leave start date
     * @param leaveRequestDto incoming request payload
     * @return persisted submitted leave response
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Override
    public LeaveResponseDTO submitLeaveRequest(Long id, LeaveRequestDTO leaveRequestDto) {
        LeaveRequestEntity leaveRequest = leaveRequestRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Leave not found"));
        if (leaveRequest.getStatus() != LeaveRequestStatusEnum.DRAFT) {
            throw new IllegalStateException("Only drafts can be submitted.");
        }

        validateSubmissionPayload(leaveRequestDto);

        LeaveTypeEntity leaveType =
                leaveTypeService.resolveLeaveType(
                        leaveRequestDto.getLeaveType()
                );


        UserEntity user = userService.getAuthenticatedUser();

        Integer year = extractYear(
                leaveRequestDto.getStartDate()
        );
        LeaveRequestEntity request = LeaveRequestEntity.builder()
                .id(id)
                .user(user)
                .leaveType(leaveType)
                .startDate(leaveRequestDto.getStartDate())
                .endDate(leaveRequestDto.getEndDate())
                .reason(leaveRequestDto.getReason())
                .status(LeaveRequestStatusEnum.PENDING)
                .createdAt(LocalDateTime.now())
                .submittedAt(LocalDateTime.now())
                .year(year)
                .build();

        LeaveRequestEntity savedLeave =
                leaveRequestRepo.save(request);

        return leaveRequestMapper.mapToResponse(savedLeave);

    }


}