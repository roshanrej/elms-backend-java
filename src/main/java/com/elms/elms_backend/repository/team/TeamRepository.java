package com.elms.elms_backend.repository.team;

import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.TeamEntity;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
    boolean existsByName(String leaveTypeName);
    Optional<TeamEntity> findByName(String teamName);
    Optional<TeamEntity> findByManagerId(Long id);
}
