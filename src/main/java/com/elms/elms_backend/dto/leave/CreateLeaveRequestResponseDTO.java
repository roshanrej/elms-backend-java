package com.elms.elms_backend.dto.leave;

import com.elms.elms_backend.entity.enums.LeaveRequestActionEnum;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateLeaveRequestResponseDTO {

    private Long id;

    private String leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    private String reason;

    private LeaveRequestStatusEnum status;

    private LocalDateTime createdAt;

    private LocalDateTime submittedAt;

    private String approverName;

    private LocalDateTime decisionAt;
    private List<LeaveRequestActionEnum> allowedActions;

}
