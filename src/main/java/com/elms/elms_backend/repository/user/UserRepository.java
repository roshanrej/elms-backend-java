package com.elms.elms_backend.repository.user;

import com.elms.elms_backend.entity.RoleEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<UserEntity,Long> {
 @Query("""
         SELECT u FROM UserEntity u
         JOIN FETCH u.role
         JOIN FETCH u.department
         LEFT JOIN FETCH u.team
         WHERE u.email = :email
         """)
 Optional<UserEntity> findByEmail(@Param("email") String email);

 @Query("""
         SELECT DISTINCT u FROM UserEntity u
         JOIN FETCH u.role
         JOIN FETCH u.department
         LEFT JOIN FETCH u.team
         """)
 List<UserEntity> findAllWithAssociations();

 @Query("""
         SELECT DISTINCT u FROM UserEntity u
         JOIN FETCH u.role
         JOIN FETCH u.department
         LEFT JOIN FETCH u.team
         WHERE u.status = :status
         """)
 List<UserEntity> findByStatusWithAssociations(@Param("status") UserStatusEnum status);

 @Query("""
         SELECT DISTINCT u FROM UserEntity u
         JOIN FETCH u.role
         JOIN FETCH u.department
         LEFT JOIN FETCH u.team
         WHERE u.role = :role AND u.status = :status
         """)
 List<UserEntity> findByRoleAndStatusWithAssociations(
         @Param("role") RoleEntity role,
         @Param("status") UserStatusEnum status
 );

 List<UserEntity> findByRole(RoleEntity role);
 List<UserEntity> findByStatus(UserStatusEnum status);
 List<UserEntity> findByRoleAndStatus(RoleEntity role, UserStatusEnum status);
 long countByDepartmentId(Long departmentId);
    Optional<UserEntity> findByIdAndStatus(
            Long id,
            UserStatusEnum status
    );

 @Query("""
         SELECT u FROM UserEntity u
         JOIN FETCH u.role
         JOIN FETCH u.department
         LEFT JOIN FETCH u.team
         WHERE u.id = :id AND u.status = :status
         """)
 Optional<UserEntity> findByIdAndStatusWithAssociations(
         @Param("id") Long id,
         @Param("status") UserStatusEnum status
 );

 @Query("""
         SELECT u FROM UserEntity u
         JOIN FETCH u.role
         JOIN FETCH u.department
         LEFT JOIN FETCH u.team
         WHERE u.id = :id
         """)
 Optional<UserEntity> findByIdWithAssociations(@Param("id") Long id);

 long countByRole_NameAndStatus(RoleEnum role, UserStatusEnum status);
}



