package com.elms.elms_backend.repository.leave;

import com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO;
import com.elms.elms_backend.entity.LeavePolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeavePolicyRepository extends JpaRepository<LeavePolicyEntity,Long> {
@Query("""
Select new com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO(
lt.id,
lt.name,
lt.status,
lp.allocated_leave,
lp.year
)
From LeavePolicyEntity as lp left join lp.leaveType lt on lp.year = year
""")
    List<LeavePolicyProjectionDTO> findPoliciesByYear(@Param("year") Integer year);

}
