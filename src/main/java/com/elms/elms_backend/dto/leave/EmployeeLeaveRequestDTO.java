package com.elms.elms_backend.dto.leave;

import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Setter
@Getter
@AllArgsConstructor
public class EmployeeLeaveRequestDTO {
    private Long id;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private Integer noOfDays;
    private LeaveRequestStatusEnum status;
}
