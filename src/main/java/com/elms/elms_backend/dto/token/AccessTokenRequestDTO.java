package com.elms.elms_backend.dto.token;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenRequestDTO {
    private String refreshToken;
}
