package com.elms.elms_backend.repository.leave;

import com.elms.elms_backend.entity.LeaveTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface LeaveTypeRepository  extends JpaRepository<LeaveTypeEntity,Long> {
    boolean existsByName(String leaveTypeName);
    Optional<LeaveTypeEntity> findByName(String leaveTypeName);
}
