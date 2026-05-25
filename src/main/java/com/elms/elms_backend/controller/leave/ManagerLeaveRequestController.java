package com.elms.elms_backend.controller.leave;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.leave.CreateLeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveRequestProjectionDTO;
import com.elms.elms_backend.dto.leave.ManagerEmployeeLeaveDTO;
import com.elms.elms_backend.service.leave.LeaveRequestService;
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
    public ResponseEntity<ApiResponseDTO<List<ManagerEmployeeLeaveDTO>>> getLeaveRequests(){
        List<ManagerEmployeeLeaveDTO> employeeLeaveDTOS = leaveRequestService.getLeaveRequests();
        ApiResponseDTO<List<ManagerEmployeeLeaveDTO>> apiResponseDTO = new ApiResponseDTO<>
                (
                        true,employeeLeaveDTOS,null
                );
                return ResponseEntity.ok(apiResponseDTO);
    }
    @PostMapping("/{id}/approve-request")
    public ResponseEntity<ApiResponseDTO<LeaveRequestProjectionDTO>> approveLeaveRequest(@PathVariable Long id){
        LeaveRequestProjectionDTO responseData = leaveRequestService.approveLeaveRequest(id);
        ApiResponseDTO<LeaveRequestProjectionDTO> apiResponseDTO = new
                ApiResponseDTO<LeaveRequestProjectionDTO>(
                        true,
                responseData,
                "Leave request approved successfully"
        );
        return ResponseEntity.ok(apiResponseDTO);
    }

    @PostMapping("/{id}/reject-request")
    public ResponseEntity<ApiResponseDTO<LeaveRequestProjectionDTO>> rejectLeaveRequest(@PathVariable Long id){
        LeaveRequestProjectionDTO responseData = leaveRequestService.rejectLeaveRequest(id);
        ApiResponseDTO<LeaveRequestProjectionDTO> apiResponseDTO = new
                ApiResponseDTO<LeaveRequestProjectionDTO>(
                true,
                responseData,
                "Leave request approved successfully"
        );
        return ResponseEntity.ok(apiResponseDTO);
    }

    @PostMapping("/{id}/approve-cancel")
    public ResponseEntity<LeaveRequestProjectionDTO>
    cancelLeaveRequest(
            @PathVariable Long id

    ) {

        LeaveRequestProjectionDTO response =
                leaveRequestService
                        .approveCancelRequest(
                                id
                        );

        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/reject-cancel")
    public ResponseEntity<LeaveRequestProjectionDTO>
    rejectLeaveCancel(
            @PathVariable Long id

    ) {

        LeaveRequestProjectionDTO response =
                leaveRequestService
                        .rejectLeaveRequest(
                                id
                        );

        return ResponseEntity.ok(response);
    }

}
