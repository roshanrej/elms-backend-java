package com.elms.elms_backend.dto.leave_analytics;

import com.elms.elms_backend.dto.leave_balance.EmployeeLeaveBalanceSummaryDTO;
import com.elms.elms_backend.dto.leavepolicy.LeaveBalanceProjectionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.grammars.hql.HqlParser;

import java.time.Month;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ManagerLeaveAnalyticsProjectionDTO {
    private Integer teamApprovalRate; // utilize leave action audit
    private Integer avgTeamRemainingDays;
    private Month teamPeakLeaveMonth;
    private List<EmployeeLeaveBalanceSummaryDTO> teamLeaveBalances;
}