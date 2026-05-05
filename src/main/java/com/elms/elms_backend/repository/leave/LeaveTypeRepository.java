package com.elms.elms_backend.repository.leave;

import com.elms.elms_backend.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveTypeRepository  extends JpaRepository<LeaveType,Long> {
}
