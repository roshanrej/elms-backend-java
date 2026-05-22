package  com.elms.elms_backend.repository.leave;


import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, Long> {


    List<LeaveRequestEntity> findByEmployee(UserEntity employee);

    @Query("""
    SELECT lr
    FROM LeaveRequestEntity lr

    JOIN FETCH lr.employee e

    JOIN FETCH lr.leaveType lt

    WHERE e.manager.id = :managerId

    AND e.status =
        com.elms.elms_backend.entity.enums
        .UserStatusEnum.ACTIVE
""")
    List<LeaveRequestEntity>
    findManagerEmployeeLeaveRequests(
            Long managerId
    );
}