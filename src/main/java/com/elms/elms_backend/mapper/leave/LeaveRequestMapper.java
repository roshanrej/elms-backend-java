package com.elms.elms_backend.mapper.leave;

import com.elms.elms_backend.dto.leave.CreateLeaveRequestResponseDTO;
import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.enums.LeaveRequestActionEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LeaveRequestMapper {
    /**
     * Maps LeaveRequestEntity into response DTO.
     *
     * @param leaveRequest leave request entity
     * @return mapped response DTO
     */
    public CreateLeaveRequestResponseDTO mapToResponse(
            LeaveRequestEntity leaveRequest
    ) {

        String leaveTypeName =
                leaveRequest.getLeaveType() != null
                        ? leaveRequest.getLeaveType().getName()
                        : null;



        String approverName =
                leaveRequest.getApprover() != null
                        ? leaveRequest.getApprover().getName()
                        : null;
        List<LeaveRequestActionEnum> allowedActions = new ArrayList<LeaveRequestActionEnum>();
        return new CreateLeaveRequestResponseDTO(
                leaveRequest.getId(),

                leaveTypeName,

                leaveRequest.getStartDate(),

                leaveRequest.getEndDate(),

                leaveRequest.getReason(),

                leaveRequest.getStatus(),

                leaveRequest.getCreatedAt(),

                leaveRequest.getSubmittedAt(),

                approverName,

                leaveRequest.getDecisionAt(),

                allowedActions


        );
    }
}
