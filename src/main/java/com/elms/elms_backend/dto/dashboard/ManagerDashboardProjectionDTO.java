package com.elms.elms_backend.dto.dashboard;

import com.elms.elms_backend.dto.leave.EmployeeLeaveRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor


public class ManagerDashboardProjectionDTO {
    @Data
    public static class ManagerDashboardLeaveProjectionDTO {
        private String email;
        private String name;
        private EmployeeLeaveRequestDTO leaveRequest;

        public ManagerDashboardLeaveProjectionDTO(String email, String name, EmployeeLeaveRequestDTO employeeLeaveRequestDTO) {
            this.email = email;
            this.name = name;
            this.leaveRequest = employeeLeaveRequestDTO;
        }
    }

    //upcoming 5 leaves
    private List<ManagerDashboardLeaveProjectionDTO> upcomingLeaves;
    private Integer pendingCount;
    private Integer pendingCancelCount;

}
