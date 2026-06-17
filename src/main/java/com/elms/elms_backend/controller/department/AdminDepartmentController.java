package com.elms.elms_backend.controller.department;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.department.CreateDepartmentDTO;
import com.elms.elms_backend.dto.department.RenameDepartmentDTO;
import com.elms.elms_backend.dto.department.UpdateDepartmentStatusDTO;
import com.elms.elms_backend.service.department.DepartmentService;
import com.elms.elms_backend.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/api/departments")
public class AdminDepartmentController {

    private final DepartmentService departmentService;

    public AdminDepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<?>> getDepartments() {
        return ResponseHandler.success(
                departmentService.getAllDepartments(),
                "Departments retrieved.",
                HttpStatus.OK
        );
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponseDTO<?>> createDepartment(@RequestBody CreateDepartmentDTO request) {
        return ResponseHandler.success(
                departmentService.createDepartment(request.getName()),
                "Department created successfully.",
                HttpStatus.CREATED
        );
    }

    @PatchMapping("/{departmentId}/status")
    public ResponseEntity<ApiResponseDTO<?>> updateStatus(
            @PathVariable Long departmentId,
            @RequestBody UpdateDepartmentStatusDTO request
    ) {
        return ResponseHandler.success(
                departmentService.updateStatus(departmentId, request.getStatus()),
                "Department status updated.",
                HttpStatus.OK
        );
    }

    @PatchMapping("/{departmentId}/rename")
    public ResponseEntity<ApiResponseDTO<?>> renameDepartment(
            @PathVariable Long departmentId,
            @RequestBody RenameDepartmentDTO request
    ) {
        String newName = request.getDepartmentName() != null
                ? request.getDepartmentName()
                : request.getName();
        return ResponseHandler.success(
                departmentService.renameDepartment(departmentId, newName),
                "Department renamed successfully.",
                HttpStatus.OK
        );
    }
}