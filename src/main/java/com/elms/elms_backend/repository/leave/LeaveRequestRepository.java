package  com.elms.elms_backend.repository.leave;


import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.LeaveRequestActionEnum;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, Long> {

    @Query("""
        SELECT DISTINCT lr
        FROM LeaveRequestEntity lr
        JOIN FETCH lr.leaveType
        WHERE lr.employee = :employee
          AND lr.status NOT IN :statuses
        """)
    List<LeaveRequestEntity> findEmployeeRequestsExcludingStatuses(
            @Param("employee") UserEntity employee,
            @Param("statuses") Collection<LeaveRequestStatusEnum> statuses
    );

    @Query("""
        SELECT DISTINCT lr
        FROM LeaveRequestEntity lr
        JOIN FETCH lr.leaveType
        WHERE lr.employee = :employee
          AND lr.status IN :statuses
        """)
    List<LeaveRequestEntity> findEmployeeRequestsWithStatuses(
            @Param("employee") UserEntity employee,
            @Param("statuses") Collection<LeaveRequestStatusEnum> statuses
    );

    List<LeaveRequestEntity> findByEmployee(UserEntity employee);

    @Query("""
        SELECT lr
        FROM LeaveRequestEntity lr
        JOIN FETCH lr.leaveType
        JOIN FETCH lr.employee e
        LEFT JOIN FETCH e.team t
        LEFT JOIN FETCH t.manager
        WHERE lr.id = :id
        """)
    Optional<LeaveRequestEntity> findByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT lr
        FROM LeaveRequestEntity lr
        JOIN FETCH lr.employee e
        LEFT JOIN FETCH e.team t
        LEFT JOIN FETCH t.manager
        JOIN FETCH lr.leaveType
        WHERE e.team.manager.id = :managerId
          AND lr.status IN :statuses
          AND e.status = com.elms.elms_backend.entity.enums.UserStatusEnum.ACTIVE
        """)
    List<LeaveRequestEntity> findLeaveRequestsByManagerAndStatusIn(
            @Param("managerId") Long managerId,
            @Param("statuses") Collection<LeaveRequestStatusEnum> statuses
    );

    @Query(
            """
Select
 COALESCE(SUM(lr.noOfDays),0) from LeaveRequestEntity lr
where lr.employee = :employee and lr.leaveType = :leaveType and lr.status In (:statuses)
"""
    )
    int sumLeaveDaysByEmployeeAndLeaveTypeAndStatusIn(
            UserEntity employee,
            LeaveTypeEntity leaveType,
            Collection<LeaveRequestStatusEnum> statuses
    );
    @Query("""
    select count(lr)
    from LeaveRequestEntity lr
    where lr.employee.team.manager.id = :managerId
      and lr.status = :status
      and lr.employee.status = com.elms.elms_backend.entity.enums.UserStatusEnum.ACTIVE
""")
    int countByManagerIdAndStatus(
            Long managerId,
            LeaveRequestStatusEnum status
    );

    @Query("""
        SELECT lr
        FROM LeaveRequestEntity lr
        JOIN FETCH lr.leaveType
        JOIN FETCH lr.employee e
        WHERE e.team.manager.id = :managerId
          AND e.status = com.elms.elms_backend.entity.enums.UserStatusEnum.ACTIVE
          AND lr.status = com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum.APPROVED
          AND lr.startDate >= :today
        ORDER BY lr.startDate ASC
        """)
    List<LeaveRequestEntity> findUpcomingApprovedLeaves(
            Long managerId,
            LocalDate today,
            Pageable pageable
    );
}