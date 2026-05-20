package  com.elms.elms_backend.repository.leave;

import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, Long> {

    @Query(value = """
    SELECT COALESCE(
        SUM(
            DATEDIFF(
                lr.end_date,
                lr.start_date
            ) + 1
        ),
        0
    )
    FROM leave_requests lr
    WHERE lr.leave_type_id = :leaveTypeId
    AND lr.status = 'APPROVED' AND Year(lr.start_date) = :year
    """,
            nativeQuery = true
    )
    Integer getTotalConsumedLeaves(
            @Param("leaveTypeId")
            Long leaveTypeId,
            @Param("year")
            Integer year
    );
    List<LeaveRequestEntity> findByUser(UserEntity user);
}