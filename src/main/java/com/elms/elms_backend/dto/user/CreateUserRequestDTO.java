package com.elms.elms_backend.dto.user;

import lombok.Data;

@Data
public class CreateUserRequestDTO {
    private String name;
    private String email;
    private String password;
    private Long departmentId;
    private Long roleId;
}