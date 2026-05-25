package com.elms.elms_backend.dto.token;

import com.elms.elms_backend.entity.enums.RoleEnum;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenResponseDTO {
    private String accessToken;
    private String email;
    private RoleEnum role;
    private String username;
}
