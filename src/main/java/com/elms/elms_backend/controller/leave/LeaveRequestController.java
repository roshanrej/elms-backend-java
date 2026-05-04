package com.elms.elms_backend.controller.leave;

import com.elms.elms_backend.dto.leave.LeaveRequestDTO;
import com.elms.elms_backend.entity.LeaveRequest;
import com.elms.elms_backend.service.leave.LeaveRequestService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
public class LeaveRequestController {

    private final LeaveRequestService service;

    public LeaveRequestController(LeaveRequestService service) {
        this.service = service;
    }

    @PostMapping
    public LeaveRequest createLeave(@RequestBody LeaveRequestDTO dto) {
        return service.createLeave(dto);
    }
}