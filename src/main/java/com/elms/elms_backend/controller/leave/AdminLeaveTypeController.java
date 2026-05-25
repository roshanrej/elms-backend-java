package com.elms.elms_backend.controller.leave;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO;
import com.elms.elms_backend.dto.leavetype.CreateLeaveTypeDTO;
import com.elms.elms_backend.dto.leavetype.CreateLeaveTypeResponseDTO;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.service.leavetype.LeaveTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/api/leave-types")
public class AdminLeaveTypeController {
    private final LeaveTypeService leaveTypeService;

    public AdminLeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponseDTO<CreateLeaveTypeResponseDTO>> createLeaveType(CreateLeaveTypeDTO createLeaveTypeDTO){
        CreateLeaveTypeResponseDTO createLeaveTypeResponseDTO = leaveTypeService.createLeaveType(createLeaveTypeDTO);
        ApiResponseDTO<CreateLeaveTypeResponseDTO> apiResponseDTO = new ApiResponseDTO<>(
                true,
                createLeaveTypeResponseDTO,
                "Leave type created!"
        );
        return ResponseEntity.ok(apiResponseDTO);
    }

}
