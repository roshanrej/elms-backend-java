package  com.elms.elms_backend.repository.leave;

import com.elms.elms_backend.entity.LeaveRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, Long> {

}