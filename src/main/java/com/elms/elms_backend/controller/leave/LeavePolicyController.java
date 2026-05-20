package com.elms.elms_backend.controller.leave;

import com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import com.elms.elms_backend.service.leavepolicy.LeavePolicyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-policies")
public class LeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    public LeavePolicyController(LeavePolicyService leavePolicyService) {
        this.leavePolicyService = leavePolicyService;
    }

    @GetMapping("/current/active")
    public List<LeavePolicyProjectionDTO> getCurrentActiveLeavePolicies(){
        return leavePolicyService.getCurrentActiveLeavePolicies();
    }
    @GetMapping("/{year}")
    public List<LeavePolicyProjectionDTO> getLeavePolicies(@PathVariable Integer year){
        return leavePolicyService.getLeavePolicies(year);
    }



}
