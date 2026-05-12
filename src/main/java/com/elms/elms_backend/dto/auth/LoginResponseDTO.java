package com.elms.elms_backend.dto.auth;

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
}



