package com.elms.elms_backend.controller.user;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.service.user.UserService;
import com.elms.elms_backend.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<ApiResponseDTO<?>> getUsers() {
        return ResponseHandler.success(
                userService.getAllUsers(),
                "Users retrieved.",
                HttpStatus.OK
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDTO<?>> getActiveUsers() {
        return ResponseHandler.success(
                userService.getActiveUsers(),
                "Active users retrieved.",
                HttpStatus.OK
        );
    }

    @PatchMapping("/{userId}/role/{roleId}")
    public ResponseEntity<ApiResponseDTO<?>> assignRole(
            @PathVariable Long userId,
            @PathVariable Long roleId
    ) {
        return ResponseHandler.success(
                userService.assignRole(userId, roleId),
                "Role assigned successfully.",
                HttpStatus.OK
        );
    }

    @PatchMapping("/{userId}/team/{teamId}")
    public ResponseEntity<ApiResponseDTO<?>> assignTeam(
            @PathVariable Long userId,
            @PathVariable Long teamId
    ) {
        return ResponseHandler.success(
                userService.assignUserToTeam(userId, teamId),
                "Team assigned successfully.",
                HttpStatus.OK
        );
    }

    @PatchMapping("/{userId}/department/{departmentId}")
    public ResponseEntity<ApiResponseDTO<?>> assignDepartment(
            @PathVariable Long userId,
            @PathVariable Long departmentId
    ) {
        return ResponseHandler.success(
                userService.assignDepartment(userId, departmentId),
                "Department assigned successfully.",
                HttpStatus.OK
        );
    }
}