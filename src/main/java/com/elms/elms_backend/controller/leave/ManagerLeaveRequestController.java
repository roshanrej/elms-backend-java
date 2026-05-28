package com.elms.elms_backend.controller.leave;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.leave.LeaveRequestProjectionDTO;
import com.elms.elms_backend.dto.leave.ManagerEmployeeLeaveDTO;
import com.elms.elms_backend.service.leaverequest.LeaveRequestService;
import com.elms.elms_backend.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("manager/api/leave-requests")
public class ManagerLeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public ManagerLeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @GetMapping()
    public ResponseEntity<ApiResponseDTO<?>> getLeaveRequests(){
        List<ManagerEmployeeLeaveDTO> employeeLeaveDTOS = leaveRequestService.getLeaveRequests();

                return ResponseHandler.success(
                        employeeLeaveDTOS,
                        "Employee leave requests retrieved",
                        HttpStatus.OK
                );
    }
    @PostMapping("/{id}/approve-request")
    public ResponseEntity<ApiResponseDTO<?>> approveLeaveRequest(@PathVariable Long id){
        LeaveRequestProjectionDTO responseData = leaveRequestService.approveLeaveRequest(id);
        return ResponseHandler.success(
                responseData,
                "Leave request approved",
                HttpStatus.OK
        );
    }

    @PostMapping("/{id}/reject-request")
    public ResponseEntity<ApiResponseDTO<?>> rejectLeaveRequest(@PathVariable Long id){
        LeaveRequestProjectionDTO responseData = leaveRequestService.rejectLeaveRequest(id);
        return ResponseHandler.success(
                responseData,
                "Leave request rejected",
                HttpStatus.OK
        );
    }

    @PostMapping("/{id}/approve-cancel")
    public ResponseEntity<ApiResponseDTO<?>>
    cancelLeaveRequest(
            @PathVariable Long id

    ) {

        LeaveRequestProjectionDTO response =
                leaveRequestService
                        .approveCancelRequest(
                                id
                        );

        return ResponseHandler.success(
                response,
                "Leave cancel approved",
                HttpStatus.OK
        );
    }
    @PostMapping("/{id}/reject-cancel")
    public ResponseEntity<ApiResponseDTO<?>>
    rejectLeaveCancel(
            @PathVariable Long id

    ) {

        LeaveRequestProjectionDTO response =
                leaveRequestService
                        .rejectLeaveRequest(
                                id
                        );

        return ResponseHandler.success(
                response,
                "Leave request rejected",
                HttpStatus.OK
        );
    }

}
