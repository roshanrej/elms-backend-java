package com.elms.elms_backend.dto.team;

import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamManagerOptionDTO {
    private Long id;
    private String name;
    private String email;
    private RoleEnum role;
    private UserStatusEnum status;
}