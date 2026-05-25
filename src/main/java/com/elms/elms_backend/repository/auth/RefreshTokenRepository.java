package com.elms.elms_backend.repository.auth;

import com.elms.elms_backend.entity.RefreshTokenEntity;

import com.elms.elms_backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByToken(String token);
    Optional<RefreshTokenEntity> findByUser(UserEntity user);
}