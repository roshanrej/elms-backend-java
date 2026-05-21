package com.elms.elms_backend.controller.leave;

import com.elms.elms_backend.dto.leave.CreateLeaveRequestDTO;
import com.elms.elms_backend.dto.leave.CreateLeaveRequestResponseDTO;
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
    public List<CreateLeaveRequestResponseDTO> getEmployeeLeaveRequests(){
        return service.getEmployeeLeaveRequests();
    }
    @GetMapping("/me/drafts")
    public List<CreateLeaveRequestResponseDTO> getEmployeeLeaveDrafts(){
        return service.getEmployeeLeaveDrafts();
    }
    @PostMapping("/draft")
    public CreateLeaveRequestResponseDTO createLeaveDraft(@RequestBody CreateLeaveRequestDTO createLeaveRequestDto) {
        return service.createLeaveDraft(createLeaveRequestDto);
    }

    @PostMapping("/submit")
    public CreateLeaveRequestResponseDTO submitNewLeaveRequest(@RequestBody CreateLeaveRequestDTO createLeaveRequestDto) {
        return service.submitNewLeaveRequest(createLeaveRequestDto);
    }


    @PostMapping("/{id}/submit")
    public ResponseEntity<CreateLeaveRequestResponseDTO>
    submitLeaveRequest(
            @PathVariable Long id,
            @RequestBody
            CreateLeaveRequestDTO createLeaveRequestDto
    ) {

        CreateLeaveRequestResponseDTO response =
                service
                        .submitLeaveRequest(
                                id,
                                createLeaveRequestDto
                        );

        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/request-cancel")
    public ResponseEntity<CreateLeaveRequestResponseDTO>
    requestLeaveCancel(
            @PathVariable Long id

    ) {

        CreateLeaveRequestResponseDTO response =
                service
                        .requestLeaveCancel(
                                id
                        );

        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/approve-cancel")
    public ResponseEntity<CreateLeaveRequestResponseDTO>
    cancelLeaveRequest(
            @PathVariable Long id

    ) {

        CreateLeaveRequestResponseDTO response =
                service
                        .approveCancelRequest(
                                id
                        );

        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/reject-cancel")
    public ResponseEntity<CreateLeaveRequestResponseDTO>
    rejectLeaveCancel(
            @PathVariable Long id

    ) {

        CreateLeaveRequestResponseDTO response =
                service
                        .rejectLeaveRequest(
                                id
                        );

        return ResponseEntity.ok(response);
    }

}
