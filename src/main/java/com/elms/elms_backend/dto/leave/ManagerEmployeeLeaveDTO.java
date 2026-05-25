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
    private LeaveRequestProjectionDTO leaveRequests;
    private String employeeEmail;
    private String employeeName;

    public ManagerEmployeeLeaveDTO(LeaveRequestProjectionDTO leaveRequests, String employeeName, String employeeEmail) {
        this.leaveRequests = leaveRequests;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
    }
}

