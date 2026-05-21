package com.elms.elms_backend.repository.department;

import com.elms.elms_backend.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity,Long> {
    Optional<DepartmentEntity>  findByName(String name);
}
