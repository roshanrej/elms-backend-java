package com.elms.elms_backend.repository.leave;


import com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO;
import com.elms.elms_backend.entity.LeavePolicyEntity;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeavePolicyRepository extends JpaRepository<LeavePolicyEntity,Long> {
    @Query("""
    SELECT new com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO(
        lt.id,
        lt.name,
        lt.status,
        lp.allocatedLeave,
        lp.year
    )
    FROM LeavePolicyEntity lp
    LEFT JOIN lp.leaveType lt
    WHERE lp.year = :year
""")
    List<LeavePolicyProjectionDTO> findPoliciesByYear(
            @Param("year") Integer year
    );
    @Query("""
    SELECT new com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO(
        lt.id,
        lt.name,
        lt.status,
        lp.allocatedLeave,
        lp.year
    )
    FROM LeavePolicyEntity lp
    LEFT JOIN lp.leaveType lt
    WHERE
        lp.year = :year
        AND lt.status = :status
""")
    List<LeavePolicyProjectionDTO> findPoliciesByYearAndStatus(
            @Param("year") Integer year,
            @Param("status") LeaveTypeStatusEnum status
    );


}
