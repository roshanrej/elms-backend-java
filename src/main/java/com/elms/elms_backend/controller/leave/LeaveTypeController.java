package com.elms.elms_backend.controller.leave;

import com.elms.elms_backend.dto.leavetype.LeaveTypeResponseDTO;
import com.elms.elms_backend.service.leavetype.LeaveTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/leave-types")
public class LeaveTypeController {
    private final LeaveTypeService leaveTypeService;

    public LeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    @GetMapping("/")
    public List<LeaveTypeResponseDTO> getLeaveTypes(){
      return leaveTypeService.getLeaveTypes();
    }
}
