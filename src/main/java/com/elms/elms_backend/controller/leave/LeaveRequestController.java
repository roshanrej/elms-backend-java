package com.elms.elms_backend.controller.leave;

import com.elms.elms_backend.entity.LeaveRequest;
import com.elms.elms_backend.service.leave.LeaveRequestService;
import com.elms.elms_backend.service.leave.LeaveRequestServiceImpl;
import com.elms.elms_backend.service.leave.LeaveService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
public class LeaveRequestController {

    private final LeaveRequestService service;

    public LeaveRequestController(LeaveRequestService service) {
        this.service = service;
    }

    @PostMapping
    public LeaveRequest createLeave(@RequestBody LeaveRequest request) {
        return service.createLeave(request);
    }
}