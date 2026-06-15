package com.elms.elms_backend.controller.department;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.service.department.DepartmentService;
import com.elms.elms_backend.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}