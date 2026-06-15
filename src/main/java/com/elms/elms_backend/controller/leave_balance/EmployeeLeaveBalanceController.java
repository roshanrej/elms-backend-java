package com.elms.elms_backend.controller.leave_balance;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.leavepolicy.LeaveBalanceProjectionDTO;
import com.elms.elms_backend.service.leavebalance.LeaveBalanceService;
import com.elms.elms_backend.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("employee/api/leave-balances")
public class EmployeeLeaveBalanceController {

private final LeaveBalanceService leaveBalanceService;

    public EmployeeLeaveBalanceController(LeaveBalanceService leaveBalanceService) {
        this.leaveBalanceService = leaveBalanceService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponseDTO<?>> getEmployeeLeaveBalanceProjections(){
        List<LeaveBalanceProjectionDTO> leaveBalanceProjectionDTOS = leaveBalanceService.getEmployeeLeaveBalanceProjections();
        return ResponseHandler.success(
                leaveBalanceProjectionDTOS,
                "Leave balances retrieved",
                HttpStatus.OK
        );
    }

}
