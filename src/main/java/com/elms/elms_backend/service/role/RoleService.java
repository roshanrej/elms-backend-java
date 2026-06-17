package com.elms.elms_backend.service.role;

import com.elms.elms_backend.dto.role.RoleProjectionDTO;

import java.util.List;

public interface RoleService {
    List<RoleProjectionDTO> getAssignableRoles();
}