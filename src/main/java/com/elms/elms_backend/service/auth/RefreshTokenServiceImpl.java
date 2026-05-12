package com.elms.elms_backend.service.auth;

import com.elms.elms_backend.entity.RefreshTokenEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.repository.auth.RefreshTokenRepository;
import com.elms.elms_backend.security.JwtService;
import com.elms.elms_backend.security.UserPrincipal;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepo;
    private final JwtService jwtService;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepo,
            JwtService jwtService
    ) {
        this.refreshTokenRepo = refreshTokenRepo;
        this.jwtService = jwtService;
    }

    @Override
    public RefreshTokenEntity createRefreshToken(
            UserEntity user
    ) {

        String token = jwtService.generateRefreshToken(
                new UserPrincipal(user)
        );

        RefreshTokenEntity refreshToken =
                RefreshTokenEntity.builder()
                        .token(token)
                        .user(user)
                        .expiryDate(
                                LocalDateTime.now().plusDays(7)
                        )
                        .revoked(false)
                        .build();

        return refreshTokenRepo.save(refreshToken);
    }

    @Override
    public RefreshTokenEntity validateRefreshToken(
            String token
    ) {

        RefreshTokenEntity refreshToken =
                refreshTokenRepo.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid refresh token"
                                )
                        );

        if (refreshToken.isRevoked()) {
            throw new RuntimeException(
                    "Refresh token revoked"
            );
        }

        if (refreshToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Refresh token expired"
            );
        }

        return refreshToken;
    }
}
