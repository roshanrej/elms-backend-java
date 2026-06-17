package com.elms.elms_backend.service.user;

import com.elms.elms_backend.dto.user.CreateUserRequestDTO;
import com.elms.elms_backend.dto.user.UserProjectionDTO;

import java.util.List;

public interface SuperAdminService {
    List<UserProjectionDTO> listUsers();

    UserProjectionDTO createUser(CreateUserRequestDTO request);

    UserProjectionDTO activateUser(Long userId);

    UserProjectionDTO deactivateUser(Long userId);
}