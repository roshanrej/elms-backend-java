package com.elms.elms_backend.controller.leave;

import com.elms.elms_backend.dto.leave.LeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveResponseDTO;
import com.elms.elms_backend.service.leave.LeaveRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
public class LeaveRequestController {

    private final LeaveRequestService service;

    public LeaveRequestController(LeaveRequestService service) {
        this.service = service;
    }

    @PostMapping("/draft")
    public LeaveResponseDTO createLeaveDraft(@RequestBody LeaveRequestDTO leaveRequestDto) {
        return service.createLeaveDraft(leaveRequestDto);
    }

    @PostMapping("/submit")
    public LeaveResponseDTO submitNewLeaveRequest(@RequestBody LeaveRequestDTO leaveRequestDto) {
        return service.submitNewLeaveRequest(leaveRequestDto);
    }


    @PostMapping("/{id}/submit")
    public ResponseEntity<LeaveResponseDTO>
    submitExistingLeaveRequest(
            @PathVariable Long id,
            @RequestBody
            LeaveRequestDTO leaveRequestDto
    ) {

        LeaveResponseDTO response =
                service
                        .submitLeaveRequest(
                                id,
                                leaveRequestDto
                        );

        return ResponseEntity.ok(response);
    }
}
