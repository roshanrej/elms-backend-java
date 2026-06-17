package com.elms.elms_backend.service.user;

import com.elms.elms_backend.dto.user.UserProjectionDTO;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.entity.enums.UserStatusEnum;

import java.util.List;

public interface UserService {
    UserEntity getAuthenticatedUser();
    List<UserEntity> findByRole(RoleEnum roleName);
    List<UserProjectionDTO> getAllUsers();
    List<UserProjectionDTO> getActiveUsers();
    List<UserProjectionDTO> getUsersByRoleAndStatus(RoleEnum role, UserStatusEnum status);

    UserProjectionDTO assignUserToTeam(Long userId, Long teamId);

    UserProjectionDTO assignRole(Long userId, Long roleId);

    UserProjectionDTO assignDepartment(Long userId, Long departmentId);
}