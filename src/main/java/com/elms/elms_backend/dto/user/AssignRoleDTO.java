package com.elms.elms_backend.dto.user;

import com.elms.elms_backend.entity.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssignRoleDTO {
     private Long userId;
     private RoleEnum role;
}
