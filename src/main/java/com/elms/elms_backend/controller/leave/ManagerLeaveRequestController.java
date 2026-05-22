package com.elms.elms_backend.controller.leave;

import com.elms.elms_backend.dto.leave.CreateLeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveRequestProjectionDTO;
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
