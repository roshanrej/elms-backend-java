package com.elms.elms_backend.controller.role;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.service.role.RoleService;
import com.elms.elms_backend.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/super-admin/api/roles")
public class SuperAdminRoleController {

    private final RoleService roleService;

    public SuperAdminRoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDTO<?>> getRoles() {
        return ResponseHandler.success(
                roleService.getAssignableRoles(),
                "Roles retrieved.",
                HttpStatus.OK
        );
    }
}