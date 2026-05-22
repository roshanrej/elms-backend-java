package com.elms.elms_backend.controller.leave;

import com.elms.elms_backend.dto.leave.CreateLeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveRequestProjectionDTO;
import com.elms.elms_backend.service.leave.LeaveRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("employee/api/leave-requests")
public class EmployeeLeaveRequestController {
    private final LeaveRequestService leaveRequestService;

    public EmployeeLeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @GetMapping("/me")
    public List<LeaveRequestProjectionDTO> getLeaveRequests(){
        return leaveRequestService.getEmployeeLeaveRequests();
    }
    @GetMapping("/me/drafts")
    public List<LeaveRequestProjectionDTO> getEmployeeLeaveDrafts(){
        return leaveRequestService.getEmployeeLeaveDrafts();
    }

    @PostMapping("/draft")
    public LeaveRequestProjectionDTO createLeaveDraft(@RequestBody CreateLeaveRequestDTO createLeaveRequestDto) {
        return leaveRequestService.createLeaveDraft(createLeaveRequestDto);
    }

    @PostMapping("/submit")
    public LeaveRequestProjectionDTO submitNewLeaveRequest(@RequestBody CreateLeaveRequestDTO createLeaveRequestDto) {
        return leaveRequestService.submitNewLeaveRequest(createLeaveRequestDto);
    }


    @PostMapping("/{id}/submit")
    public ResponseEntity<LeaveRequestProjectionDTO>
    submitLeaveRequest(
            @PathVariable Long id,
            @RequestBody
            CreateLeaveRequestDTO createLeaveRequestDto
    ) {

        LeaveRequestProjectionDTO response =
                leaveRequestService
                        .submitLeaveRequest(
                                id,
                                createLeaveRequestDto
                        );

        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/request-cancel")
    public ResponseEntity<LeaveRequestProjectionDTO>
    requestLeaveCancel(
            @PathVariable Long id

    ) {

        LeaveRequestProjectionDTO response =
                leaveRequestService
                        .requestLeaveCancel(
                                id
                        );

        return ResponseEntity.ok(response);
    }
}
