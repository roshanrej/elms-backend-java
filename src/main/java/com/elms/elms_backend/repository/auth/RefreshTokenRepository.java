package com.elms.elms_backend.repository.auth;

import com.elms.elms_backend.entity.RefreshTokenEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByToken(String token);

    Optional<RefreshTokenEntity> findByUserId(Long userId);
}