package com.elms.elms_backend.service.department;

import com.elms.elms_backend.dto.department.DepartmentProjectionDTO;

import java.util.List;

public interface DepartmentService {
    List<DepartmentProjectionDTO> getAllDepartments();
}