package com.elms.elms_backend.service.leave;

import com.elms.elms_backend.dto.leave.LeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveResponseDTO;
import com.elms.elms_backend.entity.LeaveRequest;
import com.elms.elms_backend.entity.LeaveType;
import com.elms.elms_backend.entity.User;
import com.elms.elms_backend.entity.enums.LeaveActionEnum;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import com.elms.elms_backend.repository.leave.LeaveRequestRepository;
import com.elms.elms_backend.repository.leave.LeaveTypeRepository;
import com.elms.elms_backend.repository.user.UserRepository;
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

    @Override
    public LeaveResponseDTO createLeave(LeaveRequestDTO leaveRequestDto, LeaveActionEnum action) {
        if(leaveRequestDto)
        // 1. Validate (only for SUBMIT)
        if (action == LeaveActionEnum.SUBMIT) {
            if (leaveRequestDto.getLeaveTypeId() == null ||
                    leaveRequestDto.getStartDate() == null ||
                    leaveRequestDto.getEndDate() == null) {
                throw new RuntimeException("Missing required fields");
            }

            if (leaveRequestDto.getEndDate().isBefore(leaveRequestDto.getStartDate())) {
                throw new RuntimeException("Invalid date range");
            }
        }

        // 2. Fetch required entities
        User user = userRepo.findById(leaveRequestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeaveType leaveType = leaveTypeRepo.findById(leaveRequestDto.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException("Leave type not found"));

        // 3. Decide status
        LeaveRequestStatusEnum status =
                (action == LeaveActionEnum.SUBMIT)
                        ? LeaveRequestStatusEnum.PENDING
                        : LeaveRequestStatusEnum.DRAFT;

        // 4. Safe year extraction
        Integer year = (leaveRequestDto.getStartDate() != null)
                ? leaveRequestDto.getStartDate().getYear()
                : null;

        // 5. Build entity
        LeaveRequest request = LeaveRequest.builder()
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

        LeaveRequest savedLeave = leaveRequestRepo.save(request);

        // 7. Return response DTO
        return new LeaveResponseDTO(
                savedLeave.getId(),
                savedLeave.getLeaveType().getId(),
                savedLeave.getStartDate(),
                savedLeave.getEndDate(),
                savedLeave.getReason(),
                savedLeave.getStatus().toString(),
                savedLeave.getYear()
        );
    }
}