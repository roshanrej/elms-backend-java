package  com.elms.elms_backend.repository.leave;


import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.LeaveRequestActionEnum;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;


public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, Long> {

    List<LeaveRequestEntity> findByEmployeeAndStatusNotIn(
            UserEntity employee,
            Collection<LeaveRequestStatusEnum> statuses
    );

    List<LeaveRequestEntity> findByEmployeeAndStatusIn(
            UserEntity employee,
            Collection<LeaveRequestStatusEnum> statuses
    );
    List<LeaveRequestEntity> findByEmployee(UserEntity employee);

    @Query("""
    SELECT lr
    FROM LeaveRequestEntity lr

    JOIN FETCH lr.employee e

    JOIN FETCH lr.leaveType lt

    WHERE e.manager.id = :managerId and lr.status in (:statuses)

    AND e.status =
        com.elms.elms_backend.entity.enums
        .UserStatusEnum.ACTIVE
""")
    List<LeaveRequestEntity>
    findLeaveRequestsByManagerAndStatusIn (
            Long managerId,
            Collection<LeaveRequestStatusEnum> statuses
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
    where lr.employee.manager.id = :managerId
      and lr.status = :status
      and lr.employee.status = com.elms.elms_backend.entity.enums.UserStatusEnum.ACTIVE
""")
    int countByManagerIdAndStatus(
            Long managerId,
            LeaveRequestStatusEnum status
    );

    @Query("""
    select lr
    from LeaveRequestEntity lr
    join fetch lr.employee e
    where e.manager.id = :managerId
      and e.status = com.elms.elms_backend.entity.enums.UserStatusEnum.ACTIVE
      and lr.status = com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum.APPROVED
      and lr.startDate >= :today
    order by lr.startDate asc
""")
    List<LeaveRequestEntity> findUpcomingApprovedLeaves(
            Long managerId,
            LocalDate today,
            Pageable pageable
    );
}