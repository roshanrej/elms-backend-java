package com.elms.elms_backend.repository.leave;

import com.elms.elms_backend.entity.LeaveAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveAuditLogRepository extends JpaRepository<LeaveAuditLogEntity, Long> {

}
