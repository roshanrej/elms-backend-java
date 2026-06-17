package com.elms.elms_backend.dto.role;

import com.elms.elms_backend.entity.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoleProjectionDTO {
    private Long id;
    private RoleEnum name;
}