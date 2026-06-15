package com.elms.elms_backend.controller.leave_policy;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyDTO;
import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyResponseDTO;
import com.elms.elms_backend.service.leavepolicy.LeavePolicyService;
import com.elms.elms_backend.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/api/leave-policies")
public class AdminLeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    public AdminLeavePolicyController(LeavePolicyService leavePolicyService) {
        this.leavePolicyService = leavePolicyService;
    }

    @GetMapping("/{year}")
    public ResponseEntity<ApiResponseDTO<?>> getLeavePolicies(@PathVariable Integer year) {
        return ResponseHandler.success(
                leavePolicyService.getLeavePolicies(year),
                "Leave policies retrieved.",
                HttpStatus.OK
        );
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponseDTO<?>> createLeavePolicy(
            @RequestBody CreateLeavePolicyDTO createLeavePolicyDTO
    ) {
        CreateLeavePolicyResponseDTO response =
                leavePolicyService.createLeavePolicy(createLeavePolicyDTO);
        return ResponseHandler.success(
                response,
                "Leave policy created.",
                HttpStatus.CREATED
        );
    }
}