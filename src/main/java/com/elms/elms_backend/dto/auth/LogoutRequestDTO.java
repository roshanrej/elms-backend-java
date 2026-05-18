package com.elms.elms_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogoutRequestDTO {
    String refreshToken;
}
