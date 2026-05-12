package com.elms.elms_backend.service.auth;

import com.elms.elms_backend.entity.RefreshTokenEntity;
import com.elms.elms_backend.entity.UserEntity;

public interface RefreshTokenService {
    RefreshTokenEntity createRefreshToken(UserEntity user);

    RefreshTokenEntity validateRefreshToken(String token);
}
