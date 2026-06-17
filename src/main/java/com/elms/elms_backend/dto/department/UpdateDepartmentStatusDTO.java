package com.elms.elms_backend.dto.department;

import com.elms.elms_backend.entity.enums.DepartmentStatusEnum;
import lombok.Data;

@Data
public class UpdateDepartmentStatusDTO {
    private DepartmentStatusEnum status;
}