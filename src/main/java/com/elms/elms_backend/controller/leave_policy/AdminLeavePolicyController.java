package com.elms.elms_backend.controller.leave_policy;

import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyDTO;
import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyResponseDTO;
import com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO;
import com.elms.elms_backend.service.leavepolicy.LeavePolicyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/api/leave-policies")
public class AdminLeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    public AdminLeavePolicyController(LeavePolicyService leavePolicyService) {
        this.leavePolicyService = leavePolicyService;
    }


    @GetMapping("/{year}")
    public List<LeavePolicyProjectionDTO> getLeavePolicies(@PathVariable Integer year){
        return leavePolicyService.getLeavePolicies(year);
    }
    @PostMapping("/create")
    public CreateLeavePolicyResponseDTO createLeavePolicy(@RequestBody CreateLeavePolicyDTO createLeavePolicyDTO){
     return leavePolicyService.createLeavePolicy(createLeavePolicyDTO);
    }



}
