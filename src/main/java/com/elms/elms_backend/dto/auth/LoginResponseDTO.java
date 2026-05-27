package com.elms.elms_backend.dto.auth;

import com.elms.elms_backend.dto.user.UserContextDTO;
import com.elms.elms_backend.entity.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data

public class LoginResponseDTO {
    private UserContextDTO user;
    private String accessToken;
    private String refreshToken;

    public LoginResponseDTO(UserContextDTO userContextDTO, String accessToken, String refreshToken) {
        this.user = userContextDTO;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;

    }

}



