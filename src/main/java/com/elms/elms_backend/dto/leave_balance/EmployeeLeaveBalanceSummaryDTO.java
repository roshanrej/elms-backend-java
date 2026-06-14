package com.elms.elms_backend.dto.leave_balance;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLeaveBalanceSummaryDTO {
private Long id;
private String name;
private Integer totalAllocated;
private Integer totalRemaining;
}
