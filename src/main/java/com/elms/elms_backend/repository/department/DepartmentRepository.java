package com.elms.elms_backend.repository.department;

import com.elms.elms_backend.entity.DepartmentEntity;
import com.elms.elms_backend.entity.enums.DepartmentStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity,Long> {
    Optional<DepartmentEntity>  findByName(String name);
    Optional<DepartmentEntity> findByIdAndStatus(Long id, DepartmentStatusEnum status);
}
