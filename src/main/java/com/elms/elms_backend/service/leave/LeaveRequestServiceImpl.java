package com.elms.elms_backend.service.leave;

import com.elms.elms_backend.dto.leave.LeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveResponseDTO;
import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.LeaveActionEnum;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import com.elms.elms_backend.repository.leave.LeaveRequestRepository;
import com.elms.elms_backend.repository.leave.LeaveTypeRepository;
import com.elms.elms_backend.repository.user.UserRepository;
import com.elms.elms_backend.security.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;


@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepo;
    private final UserRepository userRepo;
    private final LeaveTypeRepository leaveTypeRepo;

    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepo, UserRepository userRepo, LeaveTypeRepository leaveTypeRepo) {
        this.leaveRequestRepo = leaveRequestRepo;
        this.userRepo = userRepo;
        this.leaveTypeRepo = leaveTypeRepo;

    }

    @PreAuthorize("EMPLOYEE")
    @Override
    public LeaveResponseDTO createLeave(LeaveRequestDTO leaveRequestDto) {
        LeaveTypeEntity leaveType = null;

        if (leaveRequestDto.getLeaveType() != null) {
            leaveType = leaveTypeRepo.findByName(leaveRequestDto.getLeaveType()).orElseThrow(() ->
                    new RuntimeException("Leave type not found"));
        }

        if (leaveRequestDto.getAction() == null) {
            throw new RuntimeException(
                    "Action is required"
            );
        }

        // 1. Validate (only for SUBMIT)
        if (leaveRequestDto.getAction() == LeaveActionEnum.SUBMIT) {
            if (leaveRequestDto.getStartDate() == null ||
                    leaveRequestDto.getEndDate() == null ) {
                throw new RuntimeException("Missing required fields");
            }

            if (leaveRequestDto.getEndDate().isBefore(leaveRequestDto.getStartDate())) {
                throw new RuntimeException("Invalid date range");
            }
        }

        // 2. Fetch required entities
         // Does not belong here
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        UserPrincipal principal =
                (UserPrincipal)
                        authentication.getPrincipal();

        String email =
                principal.getUsername();

        UserEntity user =
                userRepo.findByEmail(email)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );



        // 3. Decide status
        LeaveRequestStatusEnum status;

        switch (leaveRequestDto.getAction()) {

            case SUBMIT:
                status = LeaveRequestStatusEnum.PENDING;
                break;

            case DRAFT:
                status = LeaveRequestStatusEnum.DRAFT;
                break;

            default:
                throw new RuntimeException(
                        "Invalid action"
                );
        }

        // 4year extraction
        Integer year = (leaveRequestDto.getStartDate() != null)
                ? leaveRequestDto.getStartDate().getYear()
                : null;

        // 5. Build entity
        LeaveRequestEntity request = LeaveRequestEntity.builder()
                .user(user)
                .leaveType(leaveType)
                .startDate(leaveRequestDto.getStartDate())
                .endDate(leaveRequestDto.getEndDate())
                .reason(leaveRequestDto.getReason())
                .status(status)
                .createdAt(LocalDateTime.now())
                .year(year)
                .build();

        // 6. Persist

        LeaveRequestEntity savedLeave = leaveRequestRepo.save(request);

        // 7. Return response DTO
        return new LeaveResponseDTO(
                savedLeave.getId(),
                savedLeave.getLeaveType().getName(),
                savedLeave.getStartDate(),
                savedLeave.getEndDate(),
                savedLeave.getReason(),
                savedLeave.getStatus().toString(),
                savedLeave.getYear()
        );
    }
}