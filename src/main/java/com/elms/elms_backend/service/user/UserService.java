package com.elms.elms_backend.service.user;

import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.RoleEnum;

import java.util.List;

public interface UserService {
    UserEntity getAuthenticatedUser();
    List<UserEntity> findByRole(RoleEnum roleName);


}
