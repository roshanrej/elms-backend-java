package com.elms.elms_backend.controller.leave_type;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.leavetype.CreateLeaveTypeDTO;
import com.elms.elms_backend.dto.leavetype.CreateLeaveTypeResponseDTO;
import com.elms.elms_backend.dto.leavetype.LeaveTypeProjectionDTO;
import com.elms.elms_backend.service.leavetype.LeaveTypeService;
import com.elms.elms_backend.util.ResponseHandler;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/api/leave-types")
public class AdminLeaveTypeController {
    private final LeaveTypeService leaveTypeService;

    public AdminLeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponseDTO<?>> createLeaveType(@RequestBody  CreateLeaveTypeDTO createLeaveTypeDTO){
        CreateLeaveTypeResponseDTO createLeaveTypeResponseDTO = leaveTypeService.createLeaveType(createLeaveTypeDTO);

        return ResponseHandler.success(
                createLeaveTypeResponseDTO,
                "Leave type created",
                HttpStatus.CREATED
        );
    }
    @GetMapping("/active")
    public ResponseEntity<ApiResponseDTO<?>> getActiveLeaveTypes(){
        List<String> leaveTypes = leaveTypeService.getActiveLeaveTypes();
        return ResponseHandler.success(leaveTypes,"Active leave types retrieved.",HttpStatus.OK);
    }
    @GetMapping()
    public ResponseEntity<ApiResponseDTO<?>> getLeaveTypes(){
        List<LeaveTypeProjectionDTO> leaveTypes = leaveTypeService.getLeaveTypes();
        return ResponseHandler.success(leaveTypes,"Leave types retrieved.",HttpStatus.OK);
    }

}
