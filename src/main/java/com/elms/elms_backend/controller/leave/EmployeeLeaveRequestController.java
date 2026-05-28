package com.elms.elms_backend.controller.leave;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.leave.CreateLeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveRequestProjectionDTO;
import com.elms.elms_backend.service.leaverequest.LeaveRequestService;
import com.elms.elms_backend.util.ResponseHandler;
import org.springframework.http.HttpStatus;
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
    @PostMapping("/{id}/cancel-request")
    public ResponseEntity<ApiResponseDTO<?>> cancelLeaveRequest(@PathVariable Long id){
        LeaveRequestProjectionDTO response = leaveRequestService.cancelLeaveRequest(id);

        return ResponseHandler.success(
                response,
                "Leave request cancelled successfully.",
                HttpStatus.OK
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponseDTO<?>> getLeaveRequests(){
        List<LeaveRequestProjectionDTO> leaveRequests = leaveRequestService.getEmployeeLeaveRequests();
        return ResponseHandler.success(
                leaveRequests,
                "Leave requests retrieved.",
                HttpStatus.OK
        );
    }
    @GetMapping("/me/drafts")
    public ResponseEntity<ApiResponseDTO<?>> getEmployeeLeaveDrafts(){
        List<LeaveRequestProjectionDTO> leaveRequests = leaveRequestService.getEmployeeLeaveDrafts();
        return ResponseHandler.success(
                leaveRequests,
                "Leave drafts retrieved.",
                HttpStatus.OK
        );
    }
@PostMapping("/drafts/{id}/delete")
public ResponseEntity<ApiResponseDTO<?>> deleteLeaveDraft(@PathVariable Long id){
        leaveRequestService.deleteLeaveDraft(id);
        return ResponseHandler.success(null,"Leave draft deleted.",HttpStatus.OK);
}
    @PostMapping("/draft")
    public ResponseEntity<ApiResponseDTO<?>> createLeaveDraft(@RequestBody CreateLeaveRequestDTO createLeaveRequestDto) {
        LeaveRequestProjectionDTO response = leaveRequestService.createLeaveDraft(createLeaveRequestDto);
        return ResponseHandler.success(
                response,
                "Leave draft created.",
                HttpStatus.CREATED
        );
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponseDTO<?>> submitNewLeaveRequest(@RequestBody CreateLeaveRequestDTO createLeaveRequestDto) {
         LeaveRequestProjectionDTO response = leaveRequestService.submitNewLeaveRequest(createLeaveRequestDto);
         return ResponseHandler.success(
                 response,
                 "Leave request submitted for approval.",
                 HttpStatus.CREATED
         );

    }


    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponseDTO<?>>
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

        return ResponseHandler.success(
                response,
                "Leave request submitted for approval.",
                HttpStatus.OK
        );
    }
    @PostMapping("/{id}/request-cancel")
    public ResponseEntity<ApiResponseDTO<?>>
    requestLeaveCancel(
            @PathVariable Long id

    ) {

        LeaveRequestProjectionDTO response =
                leaveRequestService
                        .requestLeaveCancel(
                                id
                        );

        return ResponseHandler.success(
                response,
                "Leave cancel requested.",
                HttpStatus.OK

        );
    }
}
