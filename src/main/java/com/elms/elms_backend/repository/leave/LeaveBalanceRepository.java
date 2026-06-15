package com.elms.elms_backend.repository.leave;


import com.elms.elms_backend.dto.leave_balance.EmployeeLeaveBalanceSummaryDTO;
import com.elms.elms_backend.dto.leavepolicy.LeaveBalanceProjectionDTO;
import com.elms.elms_backend.entity.LeaveBalanceEntity;
import com.elms.elms_backend.entity.LeavePolicyEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface LeaveBalanceRepository extends JpaRepository<LeaveBalanceEntity,Long> {

   LeaveBalanceEntity findByEmployeeAndLeavePolicy(
           UserEntity user, LeavePolicyEntity leavePolicy
           );

    @Query(
            """
            select new com.elms.elms_backend.dto.leavepolicy
            .LeaveBalanceProjectionDTO(
    
                lp.leaveType.name,
    
                lp.year,
    
                lp.allocatedLeave,
    
                lb.consumedLeave,
    
                lb.remainingLeave
            )
    
            from LeaveBalanceEntity lb
    
            join lb.leavePolicy lp
    
            where lb.employee = :employee
            and lp.year = :year
            """
    )
    List<LeaveBalanceProjectionDTO>
    getLeaveBalanceProjection(

            @Param("employee")
            UserEntity employee,

            @Param("year")
            Integer year
    );

    @Query(
            """
            select new com.elms.elms_backend.dto.leavepolicy
            .LeaveBalanceProjectionDTO(
    
                lp.leaveType.name,
    
                lp.year,
    
                lp.allocatedLeave,
    
                lb.consumedLeave,
    
                lb.remainingLeave
            )
    
            from LeaveBalanceEntity lb
    
            join lb.leavePolicy lp
    
            where lb.employee.team.manager.id = :managerId
            and lp.year = :year
            """
    )
    List<LeaveBalanceProjectionDTO>
    getTeamLeaveBalanceProjection(

            @Param("managerId")
            Long managerId,

            @Param("year")
            Integer year
    );

    @Query(
            """
SELECT
    new com.elms.elms_backend.dto.leave_balance.EmployeeLeaveBalanceSummaryDTO(u.id,
    u.name,
    SUM(lb.leavePolicy.allocatedLeave) AS totalAllocated,
    SUM(lb.remainingLeave) AS totalRemaining)
FROM LeaveBalanceEntity lb
JOIN UserEntity u ON lb.employee.id = u.id
WHERE lb.employee.team.manager.id = :managerId
  AND lb.leavePolicy.year = :year
  AND u.status = com.elms.elms_backend.entity.enums.UserStatusEnum.ACTIVE
GROUP BY u.id, u.name

"""
    )
 List<EmployeeLeaveBalanceSummaryDTO> getManagerTeamLeaveBalanceSummary(
            @Param("managerId")
            Long managerId,

            @Param("year")
            Integer year
    );


}
