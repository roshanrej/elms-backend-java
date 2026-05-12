package com.elms.elms_backend.mapper.leave;

import com.elms.elms_backend.dto.leave.LeaveResponseDTO;
import com.elms.elms_backend.entity.LeaveRequestEntity;
import org.springframework.stereotype.Component;

@Component
public class LeaveRequestMapper {
    /**
     * Maps LeaveRequestEntity into response DTO.
     *
     * @param leaveRequest leave request entity
     * @return mapped response DTO
     */
    public LeaveResponseDTO mapToResponse(
            LeaveRequestEntity leaveRequest
    ) {

        String leaveTypeName =
                leaveRequest.getLeaveType() != null
                        ? leaveRequest.getLeaveType().getName()
                        : null;

        return new LeaveResponseDTO(
                leaveRequest.getId(),
                leaveTypeName,
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.getReason(),
                leaveRequest.getStatus().toString(),
                leaveRequest.getYear()
        );
    }
}
