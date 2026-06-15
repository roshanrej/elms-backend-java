package com.elms.elms_backend.dto.department;

import com.elms.elms_backend.entity.enums.DepartmentStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DepartmentProjectionDTO {
    private Long id;
    private String name;
    private DepartmentStatusEnum status;
    private long memberCount;
}