package com.elms.elms_backend.repository.user;

import com.elms.elms_backend.entity.RoleEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<UserEntity,Long> {
 Optional<UserEntity> findByEmail(String email);
 List<UserEntity> findByRole(RoleEntity role);
    Optional<UserEntity> findByIdAndStatus(
            Long id,
            UserStatusEnum status
    );
}



