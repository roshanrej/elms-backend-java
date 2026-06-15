package com.elms.elms_backend.controller.leave;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.dashboard.ManagerDashboardProjectionDTO;
import com.elms.elms_backend.dto.leave.LeaveRequestProjectionDTO;
import com.elms.elms_backend.dto.leave.ManagerEmployeeLeaveDTO;
import com.elms.elms_backend.dto.leave_analytics.ManagerLeaveAnalyticsProjectionDTO;
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

    @GetMapping("/team")
    public ResponseEntity<ApiResponseDTO<?>> getActiveLeaveRequests(){
        List<ManagerEmployeeLeaveDTO> employeeLeaveDTOS = leaveRequestService.getManagerOwnedLeaveRequests();

        return ResponseHandler.success(
                employeeLeaveDTOS,
                "Employee leave requests retrieved",
                HttpStatus.OK
        );
    }
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponseDTO<?>> approveLeaveRequest(@PathVariable Long id){
        LeaveRequestProjectionDTO responseData = leaveRequestService.approveLeaveRequest(id);
        return ResponseHandler.success(
                responseData,
                "Leave request approved",
                HttpStatus.OK
        );
    }

    @PostMapping("/{id}/reject")
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
                        .rejectCancelRequest(
                                id
                        );

        return ResponseHandler.success(
                response,
                "Leave cancel rejected",
                HttpStatus.OK
        );
    }
    @GetMapping("/dashboard-projection")
    public ResponseEntity<ApiResponseDTO<?>> getManagerDashboardProjection(){
        ManagerDashboardProjectionDTO managerDashboardProjectionDTO = leaveRequestService.getManagerDashboardProjection();
        return ResponseHandler.success(
                managerDashboardProjectionDTO,
                "Dashboard data retrieved.",
                HttpStatus.OK
        );
    }

}
