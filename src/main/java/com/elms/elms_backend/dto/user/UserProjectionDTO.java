package com.elms.elms_backend.dto.user;

import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class UserProjectionDTO {
    private Long id;
    private Long teamId;
    private String email;
    private String name;
    private RoleEnum role;
    private String department;
    private UserStatusEnum status;
}
