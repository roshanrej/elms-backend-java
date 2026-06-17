package com.elms.elms_backend.repository.leave;


import com.elms.elms_backend.dto.leavepolicy.LeaveBalanceProjectionDTO;
import com.elms.elms_backend.entity.LeaveBalanceEntity;
import com.elms.elms_backend.entity.LeavePolicyEntity;
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

    List<LeaveBalanceEntity> findByEmployee(UserEntity employee);

}
