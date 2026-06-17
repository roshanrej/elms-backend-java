package com.elms.elms_backend.service.department;

import com.elms.elms_backend.dto.department.DepartmentProjectionDTO;
import com.elms.elms_backend.entity.enums.DepartmentStatusEnum;

import java.util.List;

public interface DepartmentService {
    List<DepartmentProjectionDTO> getAllDepartments();

    DepartmentProjectionDTO createDepartment(String name);

    DepartmentProjectionDTO updateStatus(Long departmentId, DepartmentStatusEnum status);

    DepartmentProjectionDTO renameDepartment(Long departmentId, String newName);
}