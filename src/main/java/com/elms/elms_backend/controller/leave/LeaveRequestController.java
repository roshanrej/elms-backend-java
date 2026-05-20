package com.elms.elms_backend.controller.leave;

import com.elms.elms_backend.dto.leave.LeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveResponseDTO;
import com.elms.elms_backend.service.leave.LeaveRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService service;

    public LeaveRequestController(LeaveRequestService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public List<LeaveResponseDTO> getEmployeeLeaveRequests(){
        return service.getEmployeeLeaveRequests();
    }
    @GetMapping("/me/drafts")
    public List<LeaveResponseDTO> getEmployeeLeaveDrafts(){
        return service.getEmployeeLeaveDrafts();
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
    submitLeaveRequest(
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
    @PostMapping("/{id}/request-cancel")
    public ResponseEntity<LeaveResponseDTO>
    requestLeaveCancel(
            @PathVariable Long id

    ) {

        LeaveResponseDTO response =
                service
                        .requestLeaveCancel(
                                id
                        );

        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/approve-cancel")
    public ResponseEntity<LeaveResponseDTO>
    cancelLeaveRequest(
            @PathVariable Long id

    ) {

        LeaveResponseDTO response =
                service
                        .approveCancelRequest(
                                id
                        );

        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/reject-cancel")
    public ResponseEntity<LeaveResponseDTO>
    rejectLeaveCancel(
            @PathVariable Long id

    ) {

        LeaveResponseDTO response =
                service
                        .rejectLeaveRequest(
                                id
                        );

        return ResponseEntity.ok(response);
    }

}
