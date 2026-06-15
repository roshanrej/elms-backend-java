package com.elms.elms_backend.controller.user;

import com.elms.elms_backend.dto.user.UserProjectionDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/users")
public class AdminUserController {


     /**
     * Fetch all users
     */
    @GetMapping
    public ResponseEntity<List<UserProjectionDTO>> getUsers() {

        return ResponseEntity.ok(List.of());
    }


    /**
     * Fetch all users
     */
    @GetMapping
    public ResponseEntity<List<UserProjectionDTO>> getActiveUsers() {

        return ResponseEntity.ok(List.of());
    }

    /**
     * Assign role
     */
    @PatchMapping("/{userId}/role/{roleId}")
    public ResponseEntity<Void> assignRole(
            @PathVariable Long userId,
            @PathVariable Long roleId
    ) {
        return ResponseEntity.ok().build();
    }

    /**
     * Assign team
     */
    @PatchMapping("/{userId}/team/{teamId}")
    public ResponseEntity<Void> assignTeam(
            @PathVariable Long userId,
            @PathVariable Long teamId
    ) {
        return ResponseEntity.ok().build();
    }

    /**
     * Assign department
     */
    @PatchMapping("/{userId}/department/{departmentId}")
    public ResponseEntity<Void> assignDepartment(
            @PathVariable Long userId,
            @PathVariable Long departmentId
    ) {
        return ResponseEntity.ok().build();
    }
}