package com.elms.elms_backend.dto.token;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenResponseDTO {
    private String accessToken;
}
