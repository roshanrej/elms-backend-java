package com.elms.elms_backend.repository.user;


import com.elms.elms_backend.entity.RoleEntity;
import com.elms.elms_backend.entity.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RoleRepository extends JpaRepository<RoleEntity,Long> {
    Optional<RoleEntity> findByName(RoleEnum roleName);
}
