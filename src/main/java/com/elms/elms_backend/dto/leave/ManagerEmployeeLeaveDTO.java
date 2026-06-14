package com.elms.elms_backend.dto.leave;


import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Getter
@Setter

@NoArgsConstructor
public class ManagerEmployeeLeaveDTO  {
    private LeaveRequestProjectionDTO leaveRequest;
    private String employeeEmail;
    private String employeeName;
    public ManagerEmployeeLeaveDTO(LeaveRequestProjectionDTO leaveRequest, String employeeName, String employeeEmail) {
        this.leaveRequest = leaveRequest;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
    }
}

