package com.elms.elms_backend.repository.leave;


import com.elms.elms_backend.entity.LeaveBalanceEntity;
import com.elms.elms_backend.entity.LeavePolicyEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface LeaveBalanceRepository extends JpaRepository<LeaveBalanceEntity,Long> {

   LeaveBalanceEntity findByEmployeeAndLeavePolicy(
           UserEntity user, LeavePolicyEntity leavePolicy
           );




//   List<LeaveBalanceProjectionDTO>

}
