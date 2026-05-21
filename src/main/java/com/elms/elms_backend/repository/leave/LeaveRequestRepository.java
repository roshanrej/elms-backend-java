package  com.elms.elms_backend.repository.leave;

import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, Long> {


    List<LeaveRequestEntity> findByUser(UserEntity user);
}