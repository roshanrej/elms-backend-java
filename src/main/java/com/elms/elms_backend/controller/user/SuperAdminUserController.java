package com.elms.elms_backend.controller.user;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.user.CreateUserRequestDTO;
import com.elms.elms_backend.service.user.SuperAdminService;
import com.elms.elms_backend.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/super-admin/api/users")
public class SuperAdminUserController {

    private final SuperAdminService superAdminService;

    public SuperAdminUserController(SuperAdminService superAdminService) {
        this.superAdminService = superAdminService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<?>> getUsers() {
        return ResponseHandler.success(
                superAdminService.listUsers(),
                "Users retrieved.",
                HttpStatus.OK
        );
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponseDTO<?>> createUser(@RequestBody CreateUserRequestDTO request) {
        return ResponseHandler.success(
                superAdminService.createUser(request),
                "User created successfully.",
                HttpStatus.CREATED
        );
    }

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<ApiResponseDTO<?>> activateUser(@PathVariable Long userId) {
        return ResponseHandler.success(
                superAdminService.activateUser(userId),
                "User activated successfully.",
                HttpStatus.OK
        );
    }

    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponseDTO<?>> deactivateUser(@PathVariable Long userId) {
        return ResponseHandler.success(
                superAdminService.deactivateUser(userId),
                "User deactivated successfully.",
                HttpStatus.OK
        );
    }
}