package com.elms.elms_backend.dto.auth;

import com.elms.elms_backend.entity.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String name;
    private String email;
    private String role;
    private String department;
    private String accessToken;
    private String refreshToken;

    public LoginResponseDTO(String name, String email, String role, String department) {

 this.name = name;
 this.role  = role;
 this.email = email;
 this.department = department;
    }
}



