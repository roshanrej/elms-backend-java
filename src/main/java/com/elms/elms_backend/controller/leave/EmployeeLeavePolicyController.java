package com.elms.elms_backend.controller.leave;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO;
import com.elms.elms_backend.service.leavepolicy.LeavePolicyService;
import com.elms.elms_backend.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("employee/api/leave-policies")
public class EmployeeLeavePolicyController {
    private final LeavePolicyService leavePolicyService;

    public EmployeeLeavePolicyController(LeavePolicyService leavePolicyService) {
        this.leavePolicyService = leavePolicyService;
    }
    @GetMapping("/current/active")
    public ResponseEntity<ApiResponseDTO<?>> getCurrentActiveLeavePolicies(){
        List<LeavePolicyProjectionDTO> currentActiveLeavePolicies = leavePolicyService.getCurrentActiveLeavePolicies();
        return ResponseHandler.success(
                currentActiveLeavePolicies,
                "Leave policies fetched successfully.",
                HttpStatus.OK
        );
    }

}
